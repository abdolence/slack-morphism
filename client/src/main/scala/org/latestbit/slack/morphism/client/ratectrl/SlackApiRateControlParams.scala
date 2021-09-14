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

package org.latestbit.slack.morphism.client.ratectrl

import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiRateLimitedError }

import scala.concurrent.duration._
import scala.language.implicitConversions

/**
 * A rate limit definition
 * @param value elements or messages count
 * @param per time unit/interval
 */
case class SlackApiRateControlLimit( value: Int, per: FiniteDuration ) {
  require( value > 0, "Value should be more than zero" )
  require( per.toMillis > 0, "Duration should be more than zero" )

  def toRateLimitInMs(): Long = {
    per.toMillis / value
  }
}

object SlackApiRateControlLimit {
  implicit def tuple2ToLimit( tuple2: ( Int, FiniteDuration ) ) = SlackApiRateControlLimit( tuple2._1, tuple2._2 )
}

/**
 * Global Slack API throttling params
 */
case class SlackApiRateControlParams(
    globalMaxRateLimit: Option[SlackApiRateControlLimit] = None,
    workspaceMaxRateLimit: Option[SlackApiRateControlLimit] = None,
    slackApiTierLimits: Map[Int, SlackApiRateControlLimit] = Map(),
    maxDelayTimeout: Option[FiniteDuration] = None,
    maxRetries: Long = 0,
    retryFor: Set[Class[_ <: SlackApiClientError]] = Set(
      classOf[SlackApiRateLimitedError]
    )
)

object SlackApiRateControlParams {

  final val Tier1 = 1
  final val Tier2 = 2
  final val Tier3 = 3
  final val Tier4 = 4

  /**
   * Rate limits according to
   * https://api.slack.com/docs/rate-limits
   */
  object StandardLimits {

    final val TierMap = Map[Int, SlackApiRateControlLimit](
      ( Tier1, ( 1, 1.minute ) ),
      ( Tier2, ( 20, 1.minute ) ),
      ( Tier3, ( 50, 1.minute ) ),
      ( Tier4, ( 100, 1.minute ) )
    )

    final val DefaultParams = SlackApiRateControlParams(
      slackApiTierLimits = TierMap
    )

    object Specials {
      final val PostChannelMessageLimit = SlackApiRateControlLimit( 1, 1.second )
      final val IncomingHookLimit       = SlackApiRateControlLimit( 1, 1.second )
    }

  }
}
