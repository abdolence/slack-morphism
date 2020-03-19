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

import io.circe.Decoder
import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiToken }
import org.latestbit.slack.morphism.client.impl.SlackApiHttpProtocolSupport
import sttp.client.Request

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait SlackApiHttpRateControlSupport extends SlackApiHttpProtocolSupport {
  protected val throttler: SlackApiRateThrottler

  override protected def protectedSlackHttpApiRequest[RS](
      request: Request[Either[String, String], Nothing],
      methodRateControl: Option[SlackApiMethodRateControlParams]
  )(
      implicit slackApiToken: SlackApiToken,
      decoder: Decoder[RS],
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {

    throttler.throttle[RS](
      uri = request.uri,
      apiToken = Some( slackApiToken ),
      methodRateControl = methodRateControl
    ) { () =>
      super[SlackApiHttpProtocolSupport]
        .protectedSlackHttpApiRequest( request, methodRateControl )
        .transformWith( retryIfPossible[RS]( request, methodRateControl )( _ ) )
    }

  }

  private def retryIfPossible[RS](
      request: Request[Either[String, String], Nothing],
      methodRateControl: Option[SlackApiMethodRateControlParams]
  )( respTry: Try[Either[SlackApiClientError, RS]] ): Future[Either[SlackApiClientError, RS]] = {
    respTry match {
      case Success( response ) => Future.successful( response )
      case Failure( ex )       => Future.failed( ex )
    }
  }
}
