/*
 * Copyright 2020 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.latestbit.slack.morphism.client.ratectrl.impl

import java.util.concurrent.{ Executors, ScheduledExecutorService, TimeUnit }

import cats._
import cats.implicits._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.concurrent
import org.latestbit.slack.morphism.concurrent.AsyncTimerSupport
import sttp.model.Uri

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util._

abstract class StandardRateThrottler[F[_] : SlackApiClientBackend.BackendType : AsyncTimerSupport] private[ratectrl] (
    params: SlackApiRateControlParams,
    scheduledExecutor: ScheduledExecutorService
)( implicit ec: ExecutionContext )
    extends SlackApiRateThrottler[F] {

  import StandardRateThrottler._
  import org.latestbit.slack.morphism.client.compat.CollectionsImplicits._

  @volatile private var globalMaxRateMetric: RateThrottlerMetric =
    params.globalMaxRateLimit.map( toRateMetric ).orNull

  private val workspaceMaxRateMetrics: scala.collection.mutable.Map[String, RateThrottlerWorkspaceMetrics] =
    scala.collection.mutable.Map[String, RateThrottlerWorkspaceMetrics]()

  startWorkspaceMetricsCleanerService()

  private def toRateMetric( rateLimit: SlackApiRateControlLimit ) = {
    val rateLimitInMs = rateLimit.toRateLimitInMs()
    val rateLimitCapacity = rateLimit.per.toMillis / rateLimitInMs

    RateThrottlerMetric(
      available = rateLimitCapacity,
      lastUpdated = currentTimeInMs(),
      rateLimitInMs = rateLimitInMs,
      delay = 0,
      maxAvailable = rateLimitCapacity
    )
  }

  protected def currentTimeInMs(): Long

  private def createOrGetWorkspaceMetrics( workspaceId: String, now: Long ): RateThrottlerWorkspaceMetrics = {
    workspaceMaxRateMetrics.getOrElseUpdate(
      workspaceId,
      RateThrottlerWorkspaceMetrics(
        params.workspaceMaxRateLimit.map( toRateMetric ),
        params.slackApiTierLimits.map {
          case ( tier, limit ) =>
            ( tier, toRateMetric( limit ) )
        },
        Map(),
        now
      )
    )
  }

  private def startWorkspaceMetricsCleanerService() = {
    scheduledExecutor.scheduleAtFixedRate(
      () => cleanWorkspaceMetrics(),
      WORKSPACE_METRICS_CLEANER_INITIAL_DELAY_IN_SEC,
      WORKSPACE_METRICS_CLEANER_INTERVAL_IN_SEC,
      TimeUnit.SECONDS
    )
  }

  private def cleanWorkspaceMetrics() = {
    val now = currentTimeInMs()

    synchronized {
      workspaceMaxRateMetrics
        .filter {
          case ( _, metrics ) =>
            now - metrics.updated > WORKSPACE_METRICS_CLEANER_MAX_OLD_MSEC
        }
        .keys
        .foreach( workspaceMaxRateMetrics.remove )
    }
  }

  private def calcWorkspaceTierMetric(
      now: Long,
      methodRateControl: Option[SlackApiMethodRateControlParams],
      workspaceMetrics: RateThrottlerWorkspaceMetrics
  ) = {
    methodRateControl
      .flatMap( _.tier.flatMap { tier =>
        workspaceMetrics.tiers.get( tier ).map { metric => ( tier, metric.update( now ) ) }
      } )

  }

  private def calcWorkspaceSpecialLimitMetric(
      now: Long,
      methodRateControl: Option[SlackApiMethodRateControlParams],
      workspaceMetrics: RateThrottlerWorkspaceMetrics
  ) = {
    methodRateControl
      .flatMap( _.specialRateLimit.map { specialLimit =>
        val metric = workspaceMetrics.specialLimits.getOrElse(
          specialLimit.key,
          toRateMetric( specialLimit.limit )
        )
        (
          specialLimit.key,
          metric.update( now )
        )
      } )
  }

  private def calcWorkspaceDelays(
      now: Long,
      workspaceId: String,
      apiMethodUri: Option[Uri],
      methodRateControl: Option[SlackApiMethodRateControlParams]
  ): List[Long] = {
    if (params.workspaceMaxRateLimit.isEmpty &&
        params.slackApiTierLimits.isEmpty &&
        methodRateControl.forall( _.specialRateLimit.isEmpty )) {
      List()
    } else {
      val workspaceMetrics =
        createOrGetWorkspaceMetrics( workspaceId, now )

      val updatedWorkspaceGlobalMetric = workspaceMetrics.wholeWorkspaceMetric.map { metric => metric.update( now ) }
      val updatedTierMetric = calcWorkspaceTierMetric( now, methodRateControl, workspaceMetrics )
      val updatedSpecialLimitMetric = calcWorkspaceSpecialLimitMetric( now, methodRateControl, workspaceMetrics )

      workspaceMaxRateMetrics.update(
        workspaceId,
        workspaceMetrics.copy(
          wholeWorkspaceMetric = updatedWorkspaceGlobalMetric,
          tiers = updatedTierMetric
            .map {
              case ( tier, metric ) =>
                workspaceMetrics.tiers.updated( tier, metric )
            }
            .getOrElse( workspaceMetrics.tiers ),
          specialLimits = updatedSpecialLimitMetric
            .map {
              case ( key, metric ) =>
                workspaceMetrics.specialLimits.updated( key, metric )
            }
            .getOrElse( workspaceMetrics.specialLimits )
        )
      )

      List(
        updatedWorkspaceGlobalMetric,
        updatedTierMetric.map( _._2 ),
        updatedSpecialLimitMetric.map( _._2 )
      ).flatten.map( _.delay )
    }
  }

  protected def calcDelay(
      apiMethodUri: Option[Uri],
      methodRateControl: Option[SlackApiMethodRateControlParams],
      apiToken: Option[SlackApiToken]
  ): Option[Long] = {
    val now = currentTimeInMs()

    synchronized {
      (List(
        Option( globalMaxRateMetric ).map { metric =>
          globalMaxRateMetric = metric.update( now )
          globalMaxRateMetric.delay
        },
        methodRateControl.flatMap( _.methodMinRateLimitDelay.map( _.toMillis ) )
      ).flatten ++ (apiToken
        .flatMap { tokenValue =>
          tokenValue.workspaceId.map { workspaceId =>
            calcWorkspaceDelays( now, workspaceId, apiMethodUri, methodRateControl )
          }
        }
        .getOrElse( List() ) )).maxOption

    }
  }

  override def shutdown(): Unit = {
    scheduledExecutor.shutdown()
  }

  private def promiseDelayedRequest[RS](
      delay: Long,
      request: () => F[Either[SlackApiClientError, RS]]
  ): F[Either[SlackApiClientError, RS]] = {
    implicitly[concurrent.AsyncTimerSupport[F]].delayed( request, delay.millis, scheduledExecutor )
  }

  def getWorkspaceMetricsCacheSize(): Int = {
    val result =
      synchronized {
        workspaceMaxRateMetrics.size
      }
    result
  }

  private def retryIfNecessary[RS](
      uri: Uri,
      apiToken: Option[SlackApiToken],
      methodRateControl: Option[SlackApiMethodRateControlParams],
      request: () => F[Either[SlackApiClientError, RS]]
  )(
      response: Either[SlackApiClientError, RS]
  ): F[Either[SlackApiClientError, RS]] = {

    def updateMaxRetryCount(
        methodRateControl: Option[SlackApiMethodRateControlParams]
    ): SlackApiMethodRateControlParams = {
      methodRateControl
        .map( mctrl =>
          mctrl.copy( maxRetries = mctrl.maxRetries.map( _ - 1 ).orElse( Some( params.maxRetries - 1 ) ) )
        )
        .getOrElse(
          SlackApiMethodRateControlParams(
            maxRetries = Some( params.maxRetries - 1 )
          )
        )
    }

    if (params.maxRetries > 0 && methodRateControl.flatMap( _.maxRetries ).forall( _ > 0 )) {
      response match {
        case Left( ex ) if ex.isInstanceOf[SlackApiRetryableError] && params.retryFor.contains( ex.getClass ) => {
          ex match {
            case rateLimitedError: SlackApiRateLimitedError => {
              throttle(
                uri,
                apiToken,
                Some(
                  updateMaxRetryCount( methodRateControl ).copy(
                    methodMinRateLimitDelay =
                      rateLimitedError.retryAfter.map( after => FiniteDuration( after, TimeUnit.SECONDS ) )
                  )
                )
              )( request )
            }
            case _ => {
              throttle(
                uri,
                apiToken,
                Some( updateMaxRetryCount( methodRateControl ) )
              )( request )
            }
          }
        }
        case _ => Monad[F].pure( response )
      }
    } else {
      Monad[F].pure( response )
    }
  }

  override def throttle[RS](
      uri: Uri,
      apiToken: Option[SlackApiToken],
      methodRateControl: Option[SlackApiMethodRateControlParams]
  )(
      request: () => F[Either[SlackApiClientError, RS]]
  ): F[Either[SlackApiClientError, RS]] = {
    calcDelay( Some( uri ), methodRateControl, apiToken ) match {
      case Some( delay ) if delay > 0 => {

        if (methodRateControl.forall( _.methodMaxRateLimitDelay.forall( _.toMillis > delay ) ) &&
            params.maxDelayTimeout.forall( _.toMillis > delay )) {
          promiseDelayedRequest[RS]( delay, request )
            .flatMap( retryIfNecessary[RS]( uri, apiToken, methodRateControl, request ) )
        } else {
          Monad[F].pure(
            Left(
              SlackApiRateLimitMaxDelayError(
                uri,
                s"Rate method max delay exceed: ${delay}. " +
                  s"Max specified: ${methodRateControl.flatMap( _.methodMaxRateLimitDelay ).getOrElse( -1 )} (local) / " +
                  s"${params.maxDelayTimeout.getOrElse( -1 )} (global)"
              )
            )
          )
        }

      }
      case _ => request().flatMap( retryIfNecessary[RS]( uri, apiToken, methodRateControl, request ) )
    }
  }
}

object StandardRateThrottler {
  final val WORKSPACE_METRICS_CLEANER_INITIAL_DELAY_IN_SEC = 5 * 60 // 5 min delay
  final val WORKSPACE_METRICS_CLEANER_INTERVAL_IN_SEC = 2 * 60 // 2 min interval
  final val WORKSPACE_METRICS_CLEANER_MAX_OLD_MSEC = 60 * 60 * 1000 // clean everything more than 1 hour old

}

final class StandardRateThrottlerImpl[F[_] : SlackApiClientBackend.BackendType : AsyncTimerSupport] private[ratectrl] (
    params: SlackApiRateControlParams
)( implicit ec: ExecutionContext )
    extends StandardRateThrottler[F](
      params,
      scheduledExecutor = Executors.newScheduledThreadPool( Runtime.getRuntime().availableProcessors )
    ) {
  override protected def currentTimeInMs(): Long = System.currentTimeMillis()
}
