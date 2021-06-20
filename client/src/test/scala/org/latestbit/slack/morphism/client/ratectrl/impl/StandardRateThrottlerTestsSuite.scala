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

import java.util.concurrent.{ Callable, ScheduledExecutorService, ScheduledFuture, TimeUnit }

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import sttp.client3._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._
import org.latestbit.slack.morphism.common.{ SlackAccessTokenValue, SlackTeamId }

class StandardRateThrottlerTestsSuite extends AnyFlatSpec with MockFactory {

  val paramsNoRetries = SlackApiRateControlParams(
    globalMaxRateLimit = Some( 50, 1.second ),
    workspaceMaxRateLimit = Some( 10, 1.second ),
    slackApiTierLimits = Map(
      ( SlackApiRateControlParams.Tier1, ( 5, 1.second ) ),
      ( SlackApiRateControlParams.Tier2, ( 2, 1.second ) )
    )
  )

  val paramsWithRetries = SlackApiRateControlParams(
    globalMaxRateLimit = Some( 50, 1.second ),
    workspaceMaxRateLimit = Some( 10, 1.second ),
    slackApiTierLimits = Map(
      ( SlackApiRateControlParams.Tier1, ( 5, 1.second ) ),
      ( SlackApiRateControlParams.Tier2, ( 2, 1.second ) )
    ),
    maxRetries = 3,
    retryFor = Set( classOf[SlackApiRateLimitedError] )
  )

  "StandardRateThrottler" should "limit global rate" in {

    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()

    var lastDelay = 0L

    ( scheduledExecutorMock
      .schedule[Unit] _ )
      .expects( *, *, TimeUnit.MILLISECONDS )
      .repeated( 50 )
      .times()
      .onCall { case ( _: Callable[_], delay: Long, _: TimeUnit ) =>
        assert( delay === 20 + lastDelay )
        lastDelay += 20

        val scheduledFuture = stub[ScheduledFuture[Unit]]
        ( scheduledFuture.isDone _ ).when().returns( true )
        scheduledFuture

      }

    var fakeCurrentTime = 0L

    val throttler = new StandardRateThrottler[Future]( paramsNoRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    var requestCalled = 0

    ( 1 to 100 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = None,
        methodRateControl = None
      ) { () =>
        requestCalled += 1
        Future.successful( Right( s"Valid res: ${idx}" ) )
      }
    }

    assert( requestCalled === 50 )

    fakeCurrentTime = 2000L
    requestCalled = 0

    ( 1 to 50 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = None,
        methodRateControl = None
      ) { () =>
        requestCalled += 1
        Future.successful( Right( s"Valid res: ${idx}" ) )
      }
    }

    assert( requestCalled === 50 )
  }

  it should "limit rate per workspace" in {

    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()

    ( scheduledExecutorMock
      .schedule[Unit] _ )
      .expects( *, *, TimeUnit.MILLISECONDS )
      .repeated( 10 )
      .times()

    var fakeCurrentTime = 0L

    val throttler = new StandardRateThrottler[Future]( paramsNoRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    val apiToken1 = SlackApiBotToken( SlackAccessTokenValue( "test-token-1" ), teamId = Some( SlackTeamId( "WID1" ) ) )
    val apiToken2 = SlackApiBotToken( SlackAccessTokenValue( "test-token-2" ), teamId = Some( SlackTeamId( "WID2" ) ) )

    ( 1 to 20 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    ( 1 to 10 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken2 ),
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    fakeCurrentTime = 2000

    ( 1 to 10 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = None
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

  it should "limit rate per workspace and method tier" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()

    ( scheduledExecutorMock
      .schedule[Unit] _ )
      .expects( *, *, TimeUnit.MILLISECONDS )
      .repeated( 5 )
      .times()

    val throttler = new StandardRateThrottler[Future]( paramsNoRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = 0L
    }

    val apiToken1 = SlackApiBotToken( SlackAccessTokenValue( "test-token-1" ), teamId = Some( SlackTeamId( "WID1" ) ) )

    ( 1 to 10 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier1 ) ) )
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

  it should "limit rate per workspace and method special limit" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()

    ( scheduledExecutorMock
      .schedule[Unit] _ )
      .expects( *, *, TimeUnit.MILLISECONDS )
      .repeated( 5 )
      .times()

    val throttler = new StandardRateThrottler[Future]( paramsNoRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = 0L
    }

    val apiToken1 = SlackApiBotToken( SlackAccessTokenValue( "test-token-1" ), teamId = Some( SlackTeamId( "WID1" ) ) )

    ( 1 to 10 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        apiToken = Some( apiToken1 ),
        methodRateControl = Some(
          SlackApiMethodRateControlParams(
            specialRateLimit = Some(
              SlackApiRateControlSpecialLimit(
                key = "test-key",
                ( 5, 1.second )
              )
            )
          )
        )
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }
  }

  it should "properly clear the cache with workspaces metrics" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    var cleanCommand: Runnable = null

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once().onCall {
      ( command: Runnable, _: Long, _: Long, _: TimeUnit ) =>
        cleanCommand = command

        val scheduledFuture = stub[ScheduledFuture[Unit]]
        ( scheduledFuture.isDone _ ).when().returns( true )
        scheduledFuture
    }

    var fakeCurrentTime = 0L

    val throttler = new StandardRateThrottler[Future]( paramsNoRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = fakeCurrentTime
    }

    assert( throttler.getWorkspaceMetricsCacheSize() === 0 )

    ( 1 to 10 ).foreach { idx =>
      throttler.throttle[String](
        uri"http://example.net/",
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier1 ) ) ),
        apiToken = Some(
          SlackApiBotToken(
            SlackAccessTokenValue( s"test-token-${idx}" ),
            teamId = Some( SlackTeamId( s"WID-${idx}" ) )
          )
        )
      ) { () => Future.successful( Right( s"Valid res: ${idx}" ) ) }
    }

    assert( cleanCommand !== null )
    assert( throttler.getWorkspaceMetricsCacheSize() === 10 )
    cleanCommand.run()
    assert( throttler.getWorkspaceMetricsCacheSize() === 10 )
    fakeCurrentTime = StandardRateThrottler.WorkspaceMetricsCleanerMaxOldInMs + 1
    cleanCommand.run()
    assert( throttler.getWorkspaceMetricsCacheSize() === 0 )

  }

  it should "retries request when params are specified" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()

    ( scheduledExecutorMock
      .schedule[Unit] _ )
      .expects( *, 5000, TimeUnit.MILLISECONDS )
      .repeated( 3 )
      .times()
      .onCall { case ( callable: Callable[_], _: Long, _: TimeUnit ) =>
        val scheduledFuture = stub[ScheduledFuture[Unit]]
        ( scheduledFuture.isDone _ ).when().returns( true )
        ( scheduledFuture.get: () => Unit ).when().returns {
          val _ = callable.call()
        }
        scheduledFuture
      }

    val throttler = new StandardRateThrottler[Future]( paramsWithRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = 0L
    }

    var calledTimes = 0

    assert(
      Await.result(
        throttler.throttle[String](
          uri"http://example.net/",
          apiToken = None,
          methodRateControl = None
        ) { () =>
          calledTimes += 1
          if (calledTimes > 3) {
            Future.successful( Right( "Test" ) )
          } else {
            Future.successful( Left( SlackApiRateLimitedError( uri"http://example.net/", retryAfter = Some( 5L ) ) ) )
          }
        },
        10.seconds
      )
        === Right( "Test" )
    )

  }

  it should "give up retrying requests when max reached" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()

    ( scheduledExecutorMock
      .schedule[Unit] _ )
      .expects( *, 5000, TimeUnit.MILLISECONDS )
      .repeated( 3 )
      .times()
      .onCall { case ( callable: Callable[_], _: Long, _: TimeUnit ) =>
        val scheduledFuture = stub[ScheduledFuture[Unit]]
        ( scheduledFuture.isDone _ ).when().returns( true )
        ( scheduledFuture.get: () => Unit ).when().returns {
          val _ = callable.call()
        }
        scheduledFuture
      }

    val throttler = new StandardRateThrottler[Future]( paramsWithRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = 0L
    }

    assert(
      Await.result(
        throttler.throttle[String](
          uri"http://example.net/",
          apiToken = None,
          methodRateControl = None
        ) { () =>
          Future.successful( Left( SlackApiRateLimitedError( uri"http://example.net/", retryAfter = Some( 5L ) ) ) )
        },
        10.seconds
      )
        === Left( SlackApiRateLimitedError( uri"http://example.net/", retryAfter = Some( 5L ) ) )
    )

  }

  it should "not retry on errors not specified in params" in {
    val scheduledExecutorMock = mock[ScheduledExecutorService]

    ( scheduledExecutorMock.scheduleAtFixedRate _ ).expects( *, *, *, * ).once()
    val throttler = new StandardRateThrottler[Future]( paramsWithRetries, scheduledExecutorMock ) {
      override protected def currentTimeInMs(): Long = 0L
    }

    assert(
      Await.result(
        throttler.throttle[String](
          uri"http://example.net/",
          apiToken = None,
          methodRateControl = None
        ) { () => Future.successful( Left( SlackApiEmptyResultError( uri"http://example.net/" ) ) ) },
        10.seconds
      )
        === Left( SlackApiEmptyResultError( uri"http://example.net/" ) )
    )
  }

}
