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

import scala.language.implicitConversions

import scala.concurrent.duration._

case class RateControlLimit( value: Int, per: FiniteDuration ) {
  require( value > 0, "Value should be more than zero" )
  require( per.toMillis > 0, "Duration should be more than zero" )

  def toRateLimitInMs(): Long = {
    per.toMillis / value
  }
}

object RateControlLimit {
  implicit def tuple2ToLimit( tuple2: ( Int, FiniteDuration ) ) = RateControlLimit( tuple2._1, tuple2._2 )
}

case class RateControlParams(
    globalMaxRateLimit: Option[RateControlLimit] = None,
    workspaceMaxRateLimit: Option[RateControlLimit] = None,
    maxDelayTimeout: Option[FiniteDuration] = None,
    slackApiTierLimits: Map[Int, RateControlLimit] = Map(),
    slackApiSpecialLimits: Map[String, RateControlLimit] = Map()
)

object RateControlParams {

  final val TIER_1 = 1
  final val TIER_2 = 2
  final val TIER_3 = 3
  final val TIER_4 = 4

  /**
   * https://api.slack.com/docs/rate-limits
   */
  object SlackStandardLimits {

    final val TIER_MAP = Map[Int, RateControlLimit](
      ( TIER_1, ( 1, 1.minute ) ),
      ( TIER_2, ( 20, 1.minute ) ),
      ( TIER_3, ( 50, 1.minute ) ),
      ( TIER_4, ( 100, 1.minute ) )
    )

    final val SPECIAL_LIMITS = Map[String, RateControlLimit](
      "chat.postMessage" -> (1, 1.second),
      "incoming-webhooks" -> (1, 1.second)
    )

    final val DEFAULT_PARAMS = RateControlParams(
      slackApiTierLimits = TIER_MAP,
      slackApiSpecialLimits = SPECIAL_LIMITS
    )

  }
}
