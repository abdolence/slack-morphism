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

case class RateThrottlerMetric private[ratectrl] (
    available: Long,
    lastUpdated: Long,
    rateLimitInMs: Long,
    delay: Long,
    maxAvailable: Long
) {

  def update( now: Long ): RateThrottlerMetric = {
    val timeElapsed = now - lastUpdated

    val ( arrived, newLastUpdated ) =
      if (timeElapsed >= rateLimitInMs) {
        val arrivedInTime  = timeElapsed / rateLimitInMs
        val newLastUpdated = lastUpdated + ( arrivedInTime * rateLimitInMs)
        ( arrivedInTime, newLastUpdated )
      } else
        ( 0L, lastUpdated )

    val newAvailable =
      math.min( available + arrived, maxAvailable )

    if (newAvailable > 0) {
      RateThrottlerMetric(
        newAvailable - 1,
        newLastUpdated,
        rateLimitInMs,
        0,
        maxAvailable
      )
    } else {
      val updatedTimeElapsed = now - newLastUpdated
      val delay              = rateLimitInMs - updatedTimeElapsed

      RateThrottlerMetric(
        0,
        now + delay,
        rateLimitInMs,
        delay,
        maxAvailable
      )
    }
  }
}
