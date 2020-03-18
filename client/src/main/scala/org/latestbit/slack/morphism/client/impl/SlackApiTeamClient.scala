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
import org.latestbit.slack.morphism.client.reqresp.team._

import scala.concurrent.{ ExecutionContext, Future }
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack Team API methods
 */
trait SlackApiTeamClient extends SlackApiHttpProtocolSupport {

  object team {

    /**
     * https://api.slack.com/methods/team.info
     */
    def info( req: SlackApiTeamInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiTeamInfoResponse]] = {

      http.get[SlackApiTeamInfoResponse](
        "team.info",
        Map(
          "team" -> req.team
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.TIER_3 ) ) )
      )
    }

    object profile {

      /**
       * https://api.slack.com/methods/team.profile.get
       */
      def get( req: SlackApiTeamProfileGetRequest )(
          implicit slackApiToken: SlackApiToken,
          ec: ExecutionContext
      ): Future[Either[SlackApiClientError, SlackApiTeamProfileGetResponse]] = {

        http.get[SlackApiTeamProfileGetResponse](
          "team.profile.get",
          Map(
            "visibility" -> req.visibility
          ),
          methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.TIER_3 ) ) )
        )
      }

    }

  }

}
