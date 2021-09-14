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
import org.latestbit.slack.morphism.client.impl.SlackApiHttpProtocolSupport
import org.latestbit.slack.morphism.client.{ SlackApiClientBackend, SlackApiClientError, SlackApiToken }
import sttp.client3.Request

trait SlackApiHttpRateControlSupport[F[_]] extends SlackApiHttpProtocolSupport[F] {

  protected val throttler: SlackApiRateThrottler[F]

  override protected def sendManagedSlackHttpRequest[RS](
      request: Request[Either[String, String], Any],
      methodRateControl: Option[SlackApiMethodRateControlParams],
      slackApiToken: Option[SlackApiToken]
  )( implicit
      decoder: Decoder[RS],
      backendType: SlackApiClientBackend.BackendType[F]
  ): F[Either[SlackApiClientError, RS]] = {

    throttler.throttle[RS](
      uri = request.uri,
      apiToken = slackApiToken,
      methodRateControl = methodRateControl
    ) { () =>
      super[SlackApiHttpProtocolSupport]
        .sendManagedSlackHttpRequest( request, methodRateControl, slackApiToken )
    }

  }

}
