/*
 * Copyright 2019 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

package org.latestbit.slack.morphism.client.impl

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.client.reqresp.pins._
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack Pins API methods
 */
trait SlackApiPinsClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object pins {

    /**
     * https://api.slack.com/methods/pins.add
     */
    def add( req: SlackApiPinsAddRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiPinsAddResponse]] = {

      http.post[SlackApiPinsAddRequest, SlackApiPinsAddResponse](
        "pins.add",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/pins.list
     */
    def list( req: SlackApiPinsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiPinsListResponse]] = {

      http.get[SlackApiPinsListResponse](
        "pins.list",
        Map(
          "channel" -> Option( req.channel.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/pins.remove
     */
    def remove( req: SlackApiPinsRemoveRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiPinsRemoveResponse]] = {

      http.post[SlackApiPinsRemoveRequest, SlackApiPinsRemoveResponse](
        "pins.remove",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

  }

}
