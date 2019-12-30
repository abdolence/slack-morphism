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

import io.circe.generic.auto._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.models.pins._
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Pins API methods
 */
trait SlackApiPinsClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object pins {

    /**
     * https://api.slack.com/methods/pins.add
     */
    def add( req: SlackApiPinsAddRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiPinsAddResponse]] = {

      protectedSlackHttpApiPost[SlackApiPinsAddRequest, SlackApiPinsAddResponse](
        "pins.add",
        req
      )
    }

    /**
     * https://api.slack.com/methods/pins.list
     */
    def list( req: SlackApiPinsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiPinsListResponse]] = {

      protectedSlackHttpApiGet[SlackApiPinsListResponse](
        "pins.list",
        Map(
          "channel" -> Option( req.channel )
        )
      )
    }

    /**
     * https://api.slack.com/methods/pins.remove
     */
    def remove( req: SlackApiPinsRemoveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiPinsRemoveResponse]] = {

      protectedSlackHttpApiPost[SlackApiPinsRemoveRequest, SlackApiPinsRemoveResponse](
        "pins.remove",
        req
      )
    }

  }

}
