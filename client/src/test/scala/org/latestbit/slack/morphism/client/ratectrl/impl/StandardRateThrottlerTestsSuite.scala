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

import java.util.concurrent.{ ScheduledExecutorService, ScheduledFuture, TimeUnit }

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl.{ SlackApiMethodRateControlParams, SlackApiRateControlParams }
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import sttp.client._

import scala.concurrent.Future
import scala.concurrent.duration._

class StandardRateThrottlerTestsSuite extends AnyFlatSpec with MockFactory {

  val params = SlackApiRateControlParams(
    globalMaxRateLimit = Some( 50, 1.second ),
    workspaceMaxRateLimit = Some( 10, 1.second ),
    slackApiTierLimits = Map(
      ( SlackApiRateControlParams.TIER_1, ( 5, 1.second ) ),
      ( SlackApiRateControlParams.TIER_2, ( 2, 1.second ) )
    )
  )

  "StandardRateThrottler" should "limit global rate" in {

    val scheduledExecutorMock = mock[ScheduledExecutorService]

    (scheduledExecutorMock.scheduleAtFixedRate _).expects( *, *, *, * ).once()

    var lastDelay = 0L

    (scheduledExecutorMock.scheduleWithFixedDelay _)
      .expects( *, 0, *, TimeUnit.MILLISECONDS )
      .repeated( 50 )
      .times()
      .onCall {
        case ( _: Runnable, _: Long, delay: Long, _: TimeUnit ) =>
          assert( delay === 20 + lastDelay )
          lastDelay += 20

          val scheduledFuture = stub[ScheduledFuture[Unit]]
          (scheduledFuture.isDone _).when().returns( true )
          scheduledFuture

      }

    var fakeCurrentTime = 0L

    val throttler = new StandardRateThrottler( params, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    (1 to 100).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = None,
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    fakeCurrentTime = 2000

    (1 to 50).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = None,
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

  it should "limit rate per workspace" in {

    val scheduledExecutorMock = mock[ScheduledExecutorService]

    (scheduledExecutorMock.scheduleAtFixedRate _).expects( *, *, *, * ).once()

    (scheduledExecutorMock.scheduleWithFixedDelay _)
      .expects( *, 0, *, TimeUnit.MILLISECONDS )
      .repeated( 10 )
      .times()

    var fakeCurrentTime = 0L

    val throttler = new StandardRateThrottler( params, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    val apiToken1 = SlackApiBotToken( "test-token-1", workspaceId = Some( "WID1" ) )
    val apiToken2 = SlackApiBotToken( "test-token-2", workspaceId = Some( "WID2" ) )

    (1 to 20).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    (1 to 10).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken2 ),
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    fakeCurrentTime = 2000

    (1 to 10).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

  it should "limit rate per workspace and method tier" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    (scheduledExecutorMock.scheduleAtFixedRate _).expects( *, *, *, * ).once()

    (scheduledExecutorMock.scheduleWithFixedDelay _)
      .expects( *, 0, *, TimeUnit.MILLISECONDS )
      .repeated( 5 )
      .times()

    val throttler = new StandardRateThrottler( params, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = 0L
    }

    val apiToken1 = SlackApiBotToken( "test-token-1", workspaceId = Some( "WID1" ) )

    (1 to 10).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.TIER_1 ) ) )
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

  it should "properly clear the cache with workspaces metrics" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    var cleanCommand: Runnable = null

    (scheduledExecutorMock.scheduleAtFixedRate _).expects( *, *, *, * ).once().onCall {
      ( command: Runnable, _: Long, _: Long, _: TimeUnit ) =>
        cleanCommand = command

        val scheduledFuture = stub[ScheduledFuture[Unit]]
        (scheduledFuture.isDone _).when().returns( true )
        scheduledFuture
    }

    var fakeCurrentTime = 0L

    val throttler = new StandardRateThrottler( params, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    assert( throttler.getWorkspaceMetricsCacheSize() === 0 )

    (1 to 10).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.TIER_1 ) ) ),
        apiToken = Some( SlackApiBotToken( s"test-token-${idx}", workspaceId = Some( s"WID-${idx}" ) ) )
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    assert( cleanCommand !== null )
    assert( throttler.getWorkspaceMetricsCacheSize() === 10 )
    cleanCommand.run()
    assert( throttler.getWorkspaceMetricsCacheSize() === 10 )
    fakeCurrentTime = StandardRateThrottler.WORKSPACE_METRICS_CLEANER_MAX_OLD_MSEC + 1
    cleanCommand.run()
    assert( throttler.getWorkspaceMetricsCacheSize() === 0 )

  }

}
