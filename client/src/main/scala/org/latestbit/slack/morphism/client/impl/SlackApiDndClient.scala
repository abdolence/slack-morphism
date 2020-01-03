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
import org.latestbit.slack.morphism.client.reqresp.dnd._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack DND API methods
 */
trait SlackApiDndClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object dnd {

    /**
     * https://api.slack.com/methods/dnd.endDnd
     */
    def endDnd()(
        implicit slackApiToken: SlackApiUserToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiDndEndDndResponse]] = {

      http.post[SlackApiDndEndDndRequest, SlackApiDndEndDndResponse](
        "dnd.endDnd",
        SlackApiDndEndDndRequest()
      )
    }

    /**
     * https://api.slack.com/methods/dnd.endSnooze
     */
    def endSnooze()(
        implicit slackApiToken: SlackApiUserToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiDndEndSnoozeResponse]] = {

      http.post[SlackApiDndEndSnoozeRequest, SlackApiDndEndSnoozeResponse](
        "dnd.endSnooze",
        SlackApiDndEndSnoozeRequest()
      )
    }

    /**
     * https://api.slack.com/methods/dnd.info
     */
    def info( req: SlackApiDndInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiDndInfoResponse]] = {

      http.get[SlackApiDndInfoResponse](
        "dnd.info",
        Map(
          "user" -> req.user
        )
      )
    }

    /**
     * https://api.slack.com/methods/dnd.setSnooze
     */
    def setSnooze()(
        implicit slackApiToken: SlackApiUserToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiDndSetSnoozeResponse]] = {

      http.post[SlackApiDndSetSnoozeRequest, SlackApiDndSetSnoozeResponse](
        "dnd.setSnooze",
        SlackApiDndSetSnoozeRequest()
      )
    }

    /**
     * https://api.slack.com/methods/dnd.teamInfo
     */
    def teamInfo( req: SlackApiDndTeamInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiDndTeamInfoResponse]] = {

      http.get[SlackApiDndTeamInfoResponse](
        "dnd.teamInfo",
        Map(
          "users" -> Some( req.users.mkString( "," ) )
        )
      )
    }

  }

}
