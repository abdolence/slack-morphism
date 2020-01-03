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
import org.latestbit.slack.morphism.client.reqresp.oauth._

import scala.concurrent.{ ExecutionContext, Future }
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack OAuth (v1/v2) API methods
 */
trait SlackApiOAuthClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>
  import org.latestbit.slack.morphism.ext.SttpExt._

  object oauth {

    /**
     * https://api.slack.com/methods/oauth.access
     */
    def access(
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: Option[String] = None
    )(
        implicit ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackOAuthV1AccessTokenResponse]] = {

      sendSlackRequest[SlackOAuthV1AccessTokenResponse](
        createSlackHttpApiRequest().auth
          .basic(
            user = clientId,
            password = clientSecret
          )
          .get(
            getSlackMethodAbsoluteUri( "oauth.access" )
              .param( "code", code )
              .param( "redirect_uri", redirectUri )
          )
      )
    }

    object v2 {

      /**
       * https://api.slack.com/methods/oauth.v2.access
       */
      def access(
          clientId: String,
          clientSecret: String,
          code: String,
          redirectUri: Option[String] = None
      )(
          implicit
          ec: ExecutionContext
      ): Future[Either[SlackApiClientError, SlackOAuthV2AccessTokenResponse]] = {

        sendSlackRequest[SlackOAuthV2AccessTokenResponse](
          createSlackHttpApiRequest().auth
            .basic(
              user = clientId,
              password = clientSecret
            )
            .get(
              getSlackMethodAbsoluteUri( "oauth.v2.access" )
                .param( "code", code )
                .param( "redirect_uri", redirectUri )
            )
        )
      }
    }
  }

}
