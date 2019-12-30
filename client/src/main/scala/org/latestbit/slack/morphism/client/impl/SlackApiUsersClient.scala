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
import org.latestbit.slack.morphism.client.models.channels.SlackChannelInfo
import org.latestbit.slack.morphism.client.models.users._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Users API methods
 */
trait SlackApiUsersClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object users {

    /**
     * https://api.slack.com/methods/users.conversations
     */
    def conversations( req: SlackApiUsersConversationsRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersConversationsResponse]] = {

      protectedSlackHttpApiGet[SlackApiUsersConversationsResponse](
        "users.conversations",
        Map(
          "cursor" -> req.cursor,
          "exclude_archived" -> req.exclude_archived.map( _.toString() ),
          "limit" -> req.limit.map( _.toString() ),
          "types" -> req.types.map( _.mkString( "," ) ),
          "user" -> req.user
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.list
     */
    def conversationsScroller( req: SlackApiUsersConversationsRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackChannelInfo, String] = {
      new SlackApiResponseScroller[SlackChannelInfo, String](
        initialLoader = { () =>
          conversations( req )
        },
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
    def getPresence( req: SlackApiUsersGetPresenceRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersGetPresenceResponse]] = {

      protectedSlackHttpApiGet[SlackApiUsersGetPresenceResponse](
        "users.getPresence",
        Map(
          "user" -> Option( req.user )
        )
      )
    }

    /**
     * https://api.slack.com/methods/users.identity
     */
    def identity()(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersIdentityResponse]] = {

      protectedSlackHttpApiGet[SlackApiUsersIdentityResponse](
        "users.identity",
        Map[String, Option[String]]()
      )
    }

    /**
     * https://api.slack.com/methods/users.info
     */
    def info( req: SlackApiUsersInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersInfoResponse]] = {

      protectedSlackHttpApiGet[SlackApiUsersInfoResponse](
        "users.info",
        Map(
          "user" -> Option( req.user ),
          "include_locale" -> req.include_locale.map( _.toString() )
        )
      )
    }

    /**
     * https://api.slack.com/methods/users.list
     */
    def list( req: SlackApiUsersListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersListResponse]] = {

      protectedSlackHttpApiGet[SlackApiUsersListResponse](
        "users.list",
        Map(
          "cursor" -> req.cursor,
          "include_locale" -> req.include_locale.map( _.toString() ),
          "limit" -> req.limit.map( _.toString() )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.list
     */
    def listScroller( req: SlackApiUsersListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackUserInfo, String] = {
      new SlackApiResponseScroller[SlackUserInfo, String](
        initialLoader = { () =>
          list( req )
        },
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
    def lookupByEmail( req: SlackApiUsersLookupByEmailRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersLookupByEmailResponse]] = {

      protectedSlackHttpApiGet[SlackApiUsersLookupByEmailResponse](
        "users.lookupByEmail",
        Map(
          "email" -> Option( req.email )
        )
      )
    }

    /**
     * https://api.slack.com/methods/users.setPresence
     */
    def setPresence( req: SlackApiUsersSetPresenceRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiUsersSetPresenceResponse]] = {

      protectedSlackHttpApiPost[SlackApiUsersSetPresenceRequest, SlackApiUsersSetPresenceResponse](
        "users.setPresence",
        req
      )
    }

    object profile {

      /**
       * https://api.slack.com/methods/users.profile.get
       */
      def get( req: SlackApiUsersProfileGetRequest )(
          implicit slackApiToken: SlackApiToken,
          backend: SttpBackend[Future, Nothing, NothingT],
          ec: ExecutionContext
      ): Future[Either[SlackApiError, SlackApiUsersProfileGetResponse]] = {

        protectedSlackHttpApiGet[SlackApiUsersProfileGetResponse](
          "users.profile.get",
          Map(
            "user" -> req.user,
            "include_locale" -> req.include_locale.map( _.toString() )
          )
        )
      }

      /**
       * https://api.slack.com/methods/users.profile.set
       */
      def set( req: SlackApiUsersProfileSetRequest )(
          implicit slackApiToken: SlackApiToken,
          backend: SttpBackend[Future, Nothing, NothingT],
          ec: ExecutionContext
      ): Future[Either[SlackApiError, SlackApiUsersProfileSetResponse]] = {

        protectedSlackHttpApiPost[SlackApiUsersProfileSetRequest, SlackApiUsersProfileSetResponse](
          "users.profile.set",
          req
        )
      }

    }

  }

}
