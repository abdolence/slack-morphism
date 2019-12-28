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
import org.latestbit.slack.morphism.client.models.dnd._
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack DND API methods
 */
trait SlackApiDndClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object dnd {

    /**
     * https://api.slack.com/methods/dnd.endDnd
     */
    def endDnd( req: SlackApiDndEndDndRequest )(
        implicit slackApiToken: SlackApiUserToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiDndEndDndResponse]] = {

      protectedSlackHttpApiPost[SlackApiDndEndDndRequest, SlackApiDndEndDndResponse](
        "dnd.endDnd",
        req
      )
    }

    /**
     * https://api.slack.com/methods/dnd.endSnooze
     */
    def endSnooze( req: SlackApiDndEndSnoozeRequest )(
        implicit slackApiToken: SlackApiUserToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiDndEndSnoozeResponse]] = {

      protectedSlackHttpApiPost[SlackApiDndEndSnoozeRequest, SlackApiDndEndSnoozeResponse](
        "dnd.endSnooze",
        req
      )
    }

    /**
     * https://api.slack.com/methods/dnd.info
     */
    def info( req: SlackApiDndInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiDndInfoResponse]] = {

      protectedSlackHttpApiGet[SlackApiDndInfoResponse](
        "dnd.info",
        Map(
          "user" -> req.user
        )
      )
    }

    /**
     * https://api.slack.com/methods/dnd.setSnooze
     */
    def setSnooze( req: SlackApiDndSetSnoozeRequest )(
        implicit slackApiToken: SlackApiUserToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiDndSetSnoozeResponse]] = {

      protectedSlackHttpApiPost[SlackApiDndSetSnoozeRequest, SlackApiDndSetSnoozeResponse](
        "dnd.setSnooze",
        req
      )
    }

    /**
     * https://api.slack.com/methods/dnd.teamInfo
     */
    def teamInfo( req: SlackApiDndTeamInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiDndTeamInfoResponse]] = {

      protectedSlackHttpApiGet[SlackApiDndTeamInfoResponse](
        "dnd.teamInfo",
        Map(
          "users" -> Some( req.users.mkString( "," ) )
        )
      )
    }

  }

}
