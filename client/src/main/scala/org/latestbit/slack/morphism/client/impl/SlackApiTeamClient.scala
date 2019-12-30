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
import org.latestbit.slack.morphism.client.models.team._
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Team API methods
 */
trait SlackApiTeamClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object team {

    /**
     * https://api.slack.com/methods/team.info
     */
    def info( req: SlackApiTeamInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiTeamInfoResponse]] = {

      protectedSlackHttpApiGet[SlackApiTeamInfoResponse](
        "team.info",
        Map(
          "team" -> req.team
        )
      )
    }

    object profile {

      /**
       * https://api.slack.com/methods/team.profile.get
       */
      def get( req: SlackApiTeamProfileGetRequest )(
          implicit slackApiToken: SlackApiToken,
          backend: SttpBackend[Future, Nothing, NothingT],
          ec: ExecutionContext
      ): Future[Either[SlackApiError, SlackApiTeamProfileGetResponse]] = {

        protectedSlackHttpApiGet[SlackApiTeamProfileGetResponse](
          "team.profile.get",
          Map(
            "visibility" -> req.visibility
          )
        )
      }

    }

  }

}
