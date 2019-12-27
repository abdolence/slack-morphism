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
import org.latestbit.slack.morphism.client.models.conversations._
import org.latestbit.slack.morphism.client.models.messages.SlackMessage
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Conversations API methods
 */
trait SlackApiConversationsClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object conversations {

    /**
     * https://api.slack.com/methods/conversations.archive
     */
    def archive( req: SlackApiConversationsArchiveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsArchiveResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsArchiveRequest,
        SlackApiConversationsArchiveResponse
      ](
        "conversations.archive",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.close
     */
    def close( req: SlackApiConversationsCloseRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsCloseResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsCloseRequest,
        SlackApiConversationsCloseResponse
      ](
        "conversations.close",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.create
     */
    def create( req: SlackApiConversationsCreateRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsCreateResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsCreateRequest,
        SlackApiConversationsCreateResponse
      ](
        "conversations.create",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.history
     */
    def history( req: SlackApiConversationsHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsHistoryResponse]] = {

      protectedSlackHttpApiGet[
        SlackApiConversationsHistoryResponse
      ](
        "conversations.history",
        Map(
          "channel" -> Option( req.channel ),
          "cursor" -> req.cursor,
          "inclusive" -> req.inclusive.map( _.toString ),
          "latest" -> req.latest,
          "limit" -> req.latest.map( _.toString ),
          "oldest" -> req.oldest
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.history
     */
    def historyScroller( req: SlackApiConversationsHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackMessage, String] = {
      new SlackApiResponseScroller[SlackMessage, String](
        initialLoader = { () =>
          history( req )
        },
        batchLoader = { cursor =>
          history(
            SlackApiConversationsHistoryRequest(
              channel = req.channel,
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/conversations.info
     */
    def info( req: SlackApiConversationsInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsInfoResponse]] = {

      protectedSlackHttpApiGet[
        SlackApiConversationsInfoResponse
      ](
        "conversations.info",
        Map(
          "channel" -> Option( req.channel ),
          "include_locale" -> req.include_locale.map( _.toString ),
          "include_num_members" -> req.include_num_members.map( _.toString )
        )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.invite
     */
    def invite( req: SlackApiConversationsInviteRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsInviteResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsInviteRequest,
        SlackApiConversationsInviteResponse
      ](
        "conversations.invite",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.join
     */
    def join( req: SlackApiConversationsJoinRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsJoinResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsJoinRequest,
        SlackApiConversationsJoinResponse
      ](
        "conversations.join",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.kick
     */
    def kick( req: SlackApiConversationsKickRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsKickResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsKickRequest,
        SlackApiConversationsKickResponse
      ](
        "conversations.kick",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.leave
     */
    def leave( req: SlackApiConversationsLeaveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsLeaveResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsLeaveRequest,
        SlackApiConversationsLeaveResponse
      ](
        "conversations.leave",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.list
     */
    def list( req: SlackApiConversationsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsListResponse]] = {

      protectedSlackHttpApiGet[
        SlackApiConversationsListResponse
      ](
        "conversations.list",
        Map(
          "cursor" -> req.cursor,
          "exclude_archived" -> req.exclude_archived.map( _.toString ),
          "limit" -> req.limit.map( _.toString ),
          "types" -> req.types.map( _.mkString( "," ) )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.list
     */
    def listScroller( req: SlackApiConversationsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackChannelInfo, String] = {
      new SlackApiResponseScroller[SlackChannelInfo, String](
        initialLoader = { () =>
          list( req )
        },
        batchLoader = { cursor =>
          list(
            SlackApiConversationsListRequest(
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/conversations.members
     */
    def members( req: SlackApiConversationsMembersRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsMembersResponse]] = {

      protectedSlackHttpApiGet[
        SlackApiConversationsMembersResponse
      ](
        "conversations.members",
        Map(
          "channel" -> Option( req.channel ),
          "cursor" -> req.cursor,
          "limit" -> req.limit.map( _.toString )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.members
     */
    def membersScroller( req: SlackApiConversationsMembersRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[String, String] = {
      new SlackApiResponseScroller[String, String](
        initialLoader = { () =>
          members( req )
        },
        batchLoader = { cursor =>
          members(
            SlackApiConversationsMembersRequest(
              channel = req.channel,
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
	   * https://api.slack.com/methods/conversations.rename
	   */
    def rename( req: SlackApiConversationsRenameRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsRenameResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsRenameRequest,
        SlackApiConversationsRenameResponse
      ](
        "conversations.rename",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.replies
     */
    def replies( req: SlackApiConversationsRepliesRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsRepliesResponse]] = {

      protectedSlackHttpApiGet[
        SlackApiConversationsRepliesResponse
      ](
        "conversations.replies",
        Map(
          "channel" -> Option( req.channel ),
          "ts" -> Option( req.ts ),
          "cursor" -> req.cursor,
          "inclusive" -> req.inclusive.map( _.toString ),
          "latest" -> req.latest,
          "oldest" -> req.oldest,
          "limit" -> req.limit.map( _.toString )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.replies
     */
    def repliesScroller( req: SlackApiConversationsRepliesRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackMessage, String] = {
      new SlackApiResponseScroller[SlackMessage, String](
        initialLoader = { () =>
          replies( req )
        },
        batchLoader = { cursor =>
          replies(
            SlackApiConversationsRepliesRequest(
              channel = req.channel,
              ts = req.ts,
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/conversations.setPurpose
     */
    def setPurpose( req: SlackApiConversationsSetPurposeRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsSetPurposeResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsSetPurposeRequest,
        SlackApiConversationsSetPurposeResponse
      ](
        "conversations.setPurpose",
        req
      )
    }

    /**
     * https://api.slack.com/methods/conversations.setTopic
     */
    def setTopic( req: SlackApiConversationsSetTopicRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsSetTopicResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsSetTopicRequest,
        SlackApiConversationsSetTopicResponse
      ](
        "conversations.setTopic",
        req
      )
    }

    /**
	   * https://api.slack.com/methods/conversations.unarchive
	   */
    def unarchive( req: SlackApiConversationsUnarchiveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiConversationsUnarchiveResponse]] = {

      protectedSlackHttpApiPost[
        SlackApiConversationsUnarchiveRequest,
        SlackApiConversationsUnarchiveResponse
      ](
        "conversations.unarchive",
        req
      )
    }

  }

}
