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

import cats.MonadError
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.client.reqresp.apps._
import org.latestbit.slack.morphism.codecs.implicits._
import org.latestbit.slack.morphism.client.reqresp.apps.event._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.events._

/**
 * Support for Slack Apps API methods
 */
trait SlackApiAppsClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object apps {

    /**
     * https://api.slack.com/methods/apps.uninstall
     */
    def uninstall( req: SlackApiUninstallRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUninstallResponse]] = {

      http.post[SlackApiUninstallRequest, SlackApiUninstallResponse](
        "apps.uninstall",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier1 ) ) )
      )
    }

    object event {

      object authorizations {

        /**
         * https://api.slack.com/methods/apps.event.authorizations.list
         */
        def list( req: SlackApiEventAuthorizationsListRequest )( implicit
            slackApiToken: SlackApiAppToken,
            backendType: SlackApiClientBackend.BackendType[F]
        ): F[Either[SlackApiClientError, SlackApiEventAuthorizationsListResponse]] = {

          http.post[
            SlackApiEventAuthorizationsListRequest,
            SlackApiEventAuthorizationsListResponse
          ](
            "apps.event.authorizations.list",
            req,
            methodRateControl =
              Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier4 ) ) )
          )
        }

        /**
         * Scrolling support for
         * https://api.slack.com/methods/apps.event.authorizations.list
         */
        def listScroller( req: SlackApiEventAuthorizationsListRequest )( implicit
            slackApiToken: SlackApiAppToken,
            backendType: SlackApiClientBackend.BackendType[F]
        ): SlackApiResponseScroller[
          F,
          SlackEventAuthorization,
          SlackCursorId,
          SlackApiEventAuthorizationsListResponse
        ] = {
          new SlackApiResponseScroller[
            F,
            SlackEventAuthorization,
            SlackCursorId,
            SlackApiEventAuthorizationsListResponse
          ](
            initialLoader = { () => list( req ) },
            batchLoader = { cursor =>
              list(
                req.copy(
                  cursor = Some( cursor ),
                  limit = req.limit
                )
              )
            }
          )
        }
      }

    }
  }

}
