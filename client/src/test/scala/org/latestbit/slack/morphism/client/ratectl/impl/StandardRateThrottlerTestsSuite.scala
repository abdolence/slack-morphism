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

import java.util.concurrent.ScheduledExecutorService

import org.latestbit.slack.morphism.client.SlackApiEmptyResultError
import org.latestbit.slack.morphism.client.ratectl.RateControlParams
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration._
import sttp.client._

import scala.concurrent.Future

class StandardRateThrottlerTestsSuite extends AnyFlatSpec with MockFactory {

  val params = RateControlParams(
    globalMaxRateLimit = Some( 50, 1.second ),
    workspaceMaxRateLimit = Some( 10, 1.second ),
    slackApiTierLimits = Map(
      ( RateControlParams.TIER_1, ( 5, 1.second ) ),
      ( RateControlParams.TIER_1, ( 2, 1.second ) )
    )
  )

  "StandardRateThrottler" should "limit global rate" in {

    val throttlerScheduledExecutorMock = mock[ScheduledExecutorService]
    val cleanerScheduledExecutorMock = mock[ScheduledExecutorService]

    (cleanerScheduledExecutorMock.scheduleAtFixedRate _).expects( *, *, *, * ).once()
    (throttlerScheduledExecutorMock.scheduleWithFixedDelay _).expects( *, 0, *, * ).repeated( 50 ).times()

    var fakeCurrentTime = 0

    val throttler = new StandardRateThrottler( params, throttlerScheduledExecutorMock, cleanerScheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    (1 to 100).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        tier = None,
        apiToken = None,
        methodMaxDelay = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    fakeCurrentTime = 2000

    (1 to 50).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        tier = None,
        apiToken = None,
        methodMaxDelay = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

}
