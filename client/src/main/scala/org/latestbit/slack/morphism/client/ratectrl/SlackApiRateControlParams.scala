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

import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiRateLimitedError, SlackApiRetryableError }

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

  final val TIER_1 = 1
  final val TIER_2 = 2
  final val TIER_3 = 3
  final val TIER_4 = 4

  /**
   * Rate limits according to
   * https://api.slack.com/docs/rate-limits
   */
  object StandardLimits {

    final val TIER_MAP = Map[Int, SlackApiRateControlLimit](
      ( TIER_1, ( 1, 1.minute ) ),
      ( TIER_2, ( 20, 1.minute ) ),
      ( TIER_3, ( 50, 1.minute ) ),
      ( TIER_4, ( 100, 1.minute ) )
    )

    final val DEFAULT_PARAMS = SlackApiRateControlParams(
      slackApiTierLimits = TIER_MAP
    )

    object Specials {
      final val POST_CHANNEL_MESSAGE_LIMIT = SlackApiRateControlLimit( 1, 1.second )
      final val INCOMING_HOOK_LIMIT = SlackApiRateControlLimit( 1, 1.second )
    }

  }
}
