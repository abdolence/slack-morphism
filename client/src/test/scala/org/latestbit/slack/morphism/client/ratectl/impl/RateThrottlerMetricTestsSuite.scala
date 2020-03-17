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

import org.latestbit.slack.morphism.client.ratectl.RateControlLimit
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration._

class RateThrottlerMetricTestsSuite extends AnyFlatSpec {
  val rateLimit = RateControlLimit( 15, 1.seconds )
  val rateLimitInMs = rateLimit.toRateLimitInMs()
  val rateLimitCapacity = rateLimit.per.toMillis / rateLimitInMs

  val metric = RateThrottlerMetric(
    available = rateLimitCapacity,
    lastUpdated = 0,
    rateLimitInMs = rateLimitInMs,
    delay = 0,
    maxAvailable = rateLimitCapacity
  )

  "RateThrottlerMetric" should "be decreased to - 1 when update happens within rateLimitInMs" in {
    val updatedMetric = metric.update( rateLimitInMs - 1 )

    assert( updatedMetric.lastUpdated === 0 )
    assert( updatedMetric.delay === 0 )
    assert( updatedMetric.available === metric.available - 1 )
    assert( updatedMetric.rateLimitInMs === metric.rateLimitInMs )
  }

  it should "be maxAvailable -1 when update happens after rateLimitInMs" in {
    val updatedMetric = metric.update( 2000 )

    assert( updatedMetric.lastUpdated === (2000 / metric.rateLimitInMs) * metric.rateLimitInMs )
    assert( updatedMetric.delay === 0 )
    assert( updatedMetric.available === metric.maxAvailable - 1 )
    assert( updatedMetric.rateLimitInMs === metric.rateLimitInMs )
  }

  it should "be delay when update happens more than available within rateLimitInMs" in {
    val updatedMetric = (0 to metric.available.toInt).foldLeft( metric ) {
      case ( metric, _ ) =>
        metric.update( 0 )
    }

    assert( updatedMetric.lastUpdated === metric.rateLimitInMs )
    assert( updatedMetric.delay === metric.rateLimitInMs )
    assert( updatedMetric.available === 0 )

  }

  it should "not be delay when update happens more than available after rateLimitInMs" in {
    val updatedMetric = (0 to metric.available.toInt).foldLeft( metric ) {
      case ( metric, _ ) =>
        metric.update( 0 )
    }

    val updatedMetricAfterRateMs =
      updatedMetric.update( metric.rateLimitInMs * 4 )

    assert( updatedMetricAfterRateMs.lastUpdated === metric.rateLimitInMs * 4 )
    assert( updatedMetricAfterRateMs.delay === 0 )
    assert( updatedMetricAfterRateMs.available === 2 )

  }

}
