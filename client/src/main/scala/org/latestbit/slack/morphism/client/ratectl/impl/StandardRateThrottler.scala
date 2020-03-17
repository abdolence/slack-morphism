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

package org.latestbit.slack.morphism.client.ratectl.impl

import java.util.concurrent.{ Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit }

import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiRateLimitMaxDelayError, SlackApiToken }
import org.latestbit.slack.morphism.client.ratectl._
import sttp.model.Uri

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.FiniteDuration

abstract class StandardRateThrottler private[ratectl] (
    params: RateControlParams,
    throttleScheduledExecutor: ScheduledExecutorService,
    cleanerScheduledExecutor: ScheduledExecutorService
) extends RateThrottler {

  @volatile private var globalMaxRateMetric: RateThrottlerMetric =
    params.globalMaxRateLimit.map( toRateMetric ).orNull

  private val workspaceMaxRateMetrics: scala.collection.mutable.Map[String, RateThrottlerWorkspaceMetrics] =
    scala.collection.mutable.Map[String, RateThrottlerWorkspaceMetrics]()

  private final val WORKSPACE_METRICS_CLEANER_INITIAL_DELAY_IN_SEC = 5 * 60 // 5 min delay
  private final val WORKSPACE_METRICS_CLEANER_INTERVAL_IN_SEC = 2 * 60 // 2 min interval
  private final val WORKSPACE_METRICS_CLEANER_MAX_OLD_MSEC = 60 * 60 * 1000 // clean everything more than 1 hour old

  startWorkspaceMetricsCleanerService()

  private def toRateMetric( rateLimit: RateControlLimit ) = {
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
        now
      )
    )
  }

  private def startWorkspaceMetricsCleanerService() = {
    cleanerScheduledExecutor.scheduleAtFixedRate(
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

  private def calcWorkspaceDelays(
      now: Long,
      workspaceId: String,
      apiMethodUri: Option[Uri],
      apiTier: Option[Int]
  ): List[Long] = {
    if (params.workspaceMaxRateLimit.isEmpty || params.slackApiTierLimits.isEmpty) {
      List()
    } else {
      val workspaceMetrics = createOrGetWorkspaceMetrics( workspaceId, now )

      val updatedWorkspaceMetric = workspaceMetrics.wholeWorkspaceMetric.map { metric => metric.update( now ) }

      val updatedTierMetric =
        apiTier.flatMap { tier => workspaceMetrics.tiers.get( tier ).map { metric => ( tier, metric.update( now ) ) } }

      workspaceMaxRateMetrics.update(
        workspaceId,
        workspaceMetrics.copy(
          wholeWorkspaceMetric = updatedWorkspaceMetric,
          tiers = updatedTierMetric
            .map {
              case ( tier, metric ) =>
                workspaceMetrics.tiers.updated( tier, metric )
            }
            .getOrElse( workspaceMetrics.tiers )
        )
      )

      List(
        updatedWorkspaceMetric,
        updatedTierMetric.map( _._2 )
      ).flatten.map( _.delay )
    }
  }

  protected def calcDelay(
      apiMethodUri: Option[Uri],
      apiTier: Option[Int],
      apiToken: Option[SlackApiToken]
  ): Option[Long] = {
    val now = currentTimeInMs()

    synchronized {
      (List(
        Option( globalMaxRateMetric ).map { metric =>
          globalMaxRateMetric = metric.update( now )
          globalMaxRateMetric.delay
        }
      ).flatten ++ (apiToken
        .flatMap { tokenValue =>
          tokenValue.workspaceId.map { workspaceId => calcWorkspaceDelays( now, workspaceId, apiMethodUri, apiTier ) }
        }
        .getOrElse( List() ) )).maxOption

    }
  }

  override def shutdown(): Unit = {
    cleanerScheduledExecutor.shutdown()
    throttleScheduledExecutor.shutdown()
  }

  override def throttle[RS](
      uri: Uri,
      tier: Option[Int],
      apiToken: Option[SlackApiToken],
      methodMaxDelay: Option[FiniteDuration]
  )(
      request: () => Future[Either[SlackApiClientError, RS]]
  ): Future[Either[SlackApiClientError, RS]] = {
    calcDelay( Some( uri ), tier, apiToken ) match {
      case Some( delay ) if delay > 0 => {

        if (methodMaxDelay.forall( _.toMillis > delay ) && params.maxDelayTimeout.forall( _.toMillis > delay )) {
          val promise = Promise[Either[SlackApiClientError, RS]]()

          throttleScheduledExecutor.scheduleWithFixedDelay(
            () => {
              promise.completeWith( request() )
            },
            0,
            delay,
            TimeUnit.MILLISECONDS
          )

          promise.future
        } else {
          Future.successful(
            Left(
              SlackApiRateLimitMaxDelayError(
                uri,
                s"Rate method max delay exceed: ${delay}. " +
                  s"Max specified: ${methodMaxDelay.getOrElse( -1 )} (local) / " +
                  s"${params.maxDelayTimeout.getOrElse( -1 )} (global)"
              )
            )
          )
        }

      }
      case _ => request()
    }
  }
}

final class StandardRateThrottlerImpl private[ratectl] ( params: RateControlParams )
    extends StandardRateThrottler(
      params,
      throttleScheduledExecutor = Executors.newScheduledThreadPool( Runtime.getRuntime().availableProcessors ),
      cleanerScheduledExecutor = Executors.newSingleThreadScheduledExecutor()
    ) {
  override protected def currentTimeInMs(): Long = System.currentTimeMillis()
}
