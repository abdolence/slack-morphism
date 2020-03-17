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

import java.util.concurrent.{ Executors, TimeUnit }

import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiToken }
import org.latestbit.slack.morphism.client.ratectl._
import sttp.model.Uri

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.FiniteDuration

class StandardRateThrottler private[ratectl] ( params: RateControlParams ) extends RateThrottler {

  @volatile private var globalMaxRateMetric: RateThrottlerMetric =
    params.globalMaxRateLimit.map( toRateMetric ).orNull

  case class RateThrottlerWorkspaceMetrics(
      wholeWorkspaceMetric: Option[RateThrottlerMetric],
      tiers: Map[Int, RateThrottlerMetric],
      updated: Long
  )

  private val workspaceMaxRateMetrics: scala.collection.mutable.Map[String, RateThrottlerWorkspaceMetrics] =
    scala.collection.mutable.Map[String, RateThrottlerWorkspaceMetrics]()

  private val workspaceMetricsCleanerExecutor = Executors.newSingleThreadScheduledExecutor()

  private final val WORKSPACE_METRICS_CLEANER_INITIAL_DELAY_IN_SEC = 5 * 60 // 5 min delay
  private final val WORKSPACE_METRICS_CLEANER_INTERVAL_IN_SEC = 2 * 60 // 2 min interval
  private final val WORKSPACE_METRICS_CLEANER_MAX_OLD_MSEC = 60 * 60 * 1000 // clean everything more than 1 hour old

  startWorkspaceMetricsCleanerService()

  private val throttleSchedulerExecutor = Executors.newScheduledThreadPool( Runtime.getRuntime().availableProcessors );

  private def toRateLimitInMs( rateLimit: RateControlLimit ) = {
    rateLimit.per.toMillis / rateLimit.value
  }

  private def toRateMetric( rateLimit: RateControlLimit ) = {
    val rateLimitInMs = toRateLimitInMs( rateLimit )

    RateThrottlerMetric(
      available = rateLimit.per.toMillis / rateLimitInMs,
      lastUpdated = currentTimeInMs(),
      rateLimitInMs = rateLimitInMs,
      delay = 0
    )
  }

  private def currentTimeInMs(): Long = System.currentTimeMillis()

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
    workspaceMetricsCleanerExecutor.scheduleAtFixedRate(
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
  ): Option[FiniteDuration] = {
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
        .map { delayInMs => FiniteDuration( delayInMs, TimeUnit.MILLISECONDS ) }

    }
  }

  override def shutdown(): Unit = {
    workspaceMetricsCleanerExecutor.shutdown()
  }

  override def throttle[RS]( apiMethodUri: Option[Uri], apiTier: Option[Int], apiToken: Option[SlackApiToken] )(
      request: () => Future[Either[SlackApiClientError, RS]]
  ): Future[Either[SlackApiClientError, RS]] = {
    calcDelay( apiMethodUri, apiTier, apiToken ) match {
      case Some( delay ) if delay.length > 0 => {
        val promise = Promise[Either[SlackApiClientError, RS]]()

        throttleSchedulerExecutor.scheduleWithFixedDelay(
          () => {
            promise.completeWith( request() )
          },
          0,
          delay.toMillis,
          TimeUnit.MILLISECONDS
        )

        promise.future
      }
      case _ => request()
    }
  }
}
