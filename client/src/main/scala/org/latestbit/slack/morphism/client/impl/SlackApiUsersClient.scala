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
import org.latestbit.slack.morphism.client.reqresp.users._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.common.{ SlackChannelInfo, SlackCursorId, SlackUserInfo }
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack Users API methods
 */
trait SlackApiUsersClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object users {

    /**
     * https://api.slack.com/methods/users.conversations
     */
    def conversations( req: SlackApiUsersConversationsRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersConversationsResponse]] = {

      http.get[SlackApiUsersConversationsResponse](
        "users.conversations",
        Map(
          "cursor"           -> req.cursor.map( _.value ),
          "exclude_archived" -> req.exclude_archived.map( _.toString() ),
          "limit"            -> req.limit.map( _.toString() ),
          "types"            -> req.types.map( _.mkString( "," ) ),
          "user"             -> req.user.map( _.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.list
     */
    def conversationsScroller( req: SlackApiUsersConversationsRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackChannelInfo, SlackCursorId, SlackApiUsersConversationsResponse] = {
      new SlackApiResponseScroller[F, SlackChannelInfo, SlackCursorId, SlackApiUsersConversationsResponse](
        initialLoader = { () => conversations( req ) },
        batchLoader = { cursor =>
          conversations(
            SlackApiUsersConversationsRequest(
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/users.getPresence
     */
    def getPresence( req: SlackApiUsersGetPresenceRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersGetPresenceResponse]] = {

      http.get[SlackApiUsersGetPresenceResponse](
        "users.getPresence",
        Map(
          "user" -> Option( req.user.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/users.identity
     */
    def identity()( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersIdentityResponse]] = {

      http.get[SlackApiUsersIdentityResponse](
        "users.identity",
        Map[String, Option[String]](),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/users.info
     */
    def info( req: SlackApiUsersInfoRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersInfoResponse]] = {

      http.get[SlackApiUsersInfoResponse](
        "users.info",
        Map(
          "user"           -> Option( req.user.value ),
          "include_locale" -> req.include_locale.map( _.toString() )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier4 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/users.list
     */
    def list( req: SlackApiUsersListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersListResponse]] = {

      http.get[SlackApiUsersListResponse](
        "users.list",
        Map(
          "cursor"         -> req.cursor.map( _.value ),
          "include_locale" -> req.include_locale.map( _.toString() ),
          "limit"          -> req.limit.map( _.toString() )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/users.list
     */
    def listScroller( req: SlackApiUsersListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackUserInfo, SlackCursorId, SlackApiUsersListResponse] = {
      new SlackApiResponseScroller[F, SlackUserInfo, SlackCursorId, SlackApiUsersListResponse](
        initialLoader = { () => list( req ) },
        batchLoader = { cursor =>
          list(
            SlackApiUsersListRequest(
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/users.lookupByEmail
     */
    def lookupByEmail( req: SlackApiUsersLookupByEmailRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersLookupByEmailResponse]] = {

      http.get[SlackApiUsersLookupByEmailResponse](
        "users.lookupByEmail",
        Map(
          "email" -> Option( req.email )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/users.setPresence
     */
    def setPresence( req: SlackApiUsersSetPresenceRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiUsersSetPresenceResponse]] = {

      http.post[SlackApiUsersSetPresenceRequest, SlackApiUsersSetPresenceResponse](
        "users.setPresence",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    object profile {

      /**
       * https://api.slack.com/methods/users.profile.get
       */
      def get( req: SlackApiUsersProfileGetRequest )( implicit
          slackApiToken: SlackApiToken,
          backendType: SlackApiClientBackend.BackendType[F]
      ): F[Either[SlackApiClientError, SlackApiUsersProfileGetResponse]] = {

        http.get[SlackApiUsersProfileGetResponse](
          "users.profile.get",
          Map(
            "user"           -> req.user.map( _.value ),
            "include_locale" -> req.include_locale.map( _.toString() )
          ),
          methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier4 ) ) )
        )
      }

      /**
       * https://api.slack.com/methods/users.profile.set
       */
      def set( req: SlackApiUsersProfileSetRequest )( implicit
          slackApiToken: SlackApiToken,
          backendType: SlackApiClientBackend.BackendType[F]
      ): F[Either[SlackApiClientError, SlackApiUsersProfileSetResponse]] = {

        http.post[SlackApiUsersProfileSetRequest, SlackApiUsersProfileSetResponse](
          "users.profile.set",
          req,
          methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
        )
      }

    }

  }

}
