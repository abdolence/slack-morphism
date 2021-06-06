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
import org.latestbit.slack.morphism.client.reqresp.conversations._
import org.latestbit.slack.morphism.messages.SlackMessage
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack Conversations API methods
 */
trait SlackApiConversationsClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object conversations {

    /**
     * https://api.slack.com/methods/conversations.archive
     */
    def archive( req: SlackApiConversationsArchiveRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsArchiveResponse]] = {

      http.post[
        SlackApiConversationsArchiveRequest,
        SlackApiConversationsArchiveResponse
      ](
        "conversations.archive",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.close
     */
    def close( req: SlackApiConversationsCloseRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsCloseResponse]] = {

      http.post[
        SlackApiConversationsCloseRequest,
        SlackApiConversationsCloseResponse
      ](
        "conversations.close",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.create
     */
    def create( req: SlackApiConversationsCreateRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsCreateResponse]] = {

      http.post[
        SlackApiConversationsCreateRequest,
        SlackApiConversationsCreateResponse
      ](
        "conversations.create",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.history
     */
    def history( req: SlackApiConversationsHistoryRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsHistoryResponse]] = {

      http.get[
        SlackApiConversationsHistoryResponse
      ](
        "conversations.history",
        Map(
          "channel"   -> Option( req.channel.value ),
          "cursor"    -> req.cursor.map( _.value ),
          "inclusive" -> req.inclusive.map( _.toString() ),
          "latest"    -> req.latest.map( _.value ),
          "limit"     -> req.limit.map( _.toString() ),
          "oldest"    -> req.oldest.map( _.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.history
     */
    def historyScroller( req: SlackApiConversationsHistoryRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackMessage, SlackCursorId, SlackApiConversationsHistoryResponse] = {
      new SlackApiResponseScroller[F, SlackMessage, SlackCursorId, SlackApiConversationsHistoryResponse](
        initialLoader = { () => history( req ) },
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
    def info( req: SlackApiConversationsInfoRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsInfoResponse]] = {

      http.get[
        SlackApiConversationsInfoResponse
      ](
        "conversations.info",
        Map(
          "channel"             -> Option( req.channel.value ),
          "include_locale"      -> req.include_locale.map( _.toString() ),
          "include_num_members" -> req.include_num_members.map( _.toString() )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.invite
     */
    def invite( req: SlackApiConversationsInviteRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsInviteResponse]] = {

      http.post[
        SlackApiConversationsInviteRequest,
        SlackApiConversationsInviteResponse
      ](
        "conversations.invite",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.join
     */
    def join( req: SlackApiConversationsJoinRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsJoinResponse]] = {

      http.post[
        SlackApiConversationsJoinRequest,
        SlackApiConversationsJoinResponse
      ](
        "conversations.join",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.kick
     */
    def kick( req: SlackApiConversationsKickRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsKickResponse]] = {

      http.post[
        SlackApiConversationsKickRequest,
        SlackApiConversationsKickResponse
      ](
        "conversations.kick",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.leave
     */
    def leave( req: SlackApiConversationsLeaveRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsLeaveResponse]] = {

      http.post[
        SlackApiConversationsLeaveRequest,
        SlackApiConversationsLeaveResponse
      ](
        "conversations.leave",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.list
     */
    def list( req: SlackApiConversationsListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsListResponse]] = {

      http.get[
        SlackApiConversationsListResponse
      ](
        "conversations.list",
        Map(
          "cursor"           -> req.cursor.map( _.value ),
          "exclude_archived" -> req.exclude_archived.map( _.toString() ),
          "limit"            -> req.limit.map( _.toString() ),
          "types"            -> req.types.map( _.toList.map( _.value ).mkString( "," ) )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.list
     */
    def listScroller( req: SlackApiConversationsListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackChannelInfo, SlackCursorId, SlackApiConversationsListResponse] = {
      new SlackApiResponseScroller[F, SlackChannelInfo, SlackCursorId, SlackApiConversationsListResponse](
        initialLoader = { () => list( req ) },
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
    def members( req: SlackApiConversationsMembersRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsMembersResponse]] = {

      http.get[
        SlackApiConversationsMembersResponse
      ](
        "conversations.members",
        Map(
          "channel" -> Option( req.channel.value ),
          "cursor"  -> req.cursor.map( _.value ),
          "limit"   -> req.limit.map( _.toString() )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier4 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.members
     */
    def membersScroller( req: SlackApiConversationsMembersRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackUserId, SlackCursorId, SlackApiConversationsMembersResponse] = {
      new SlackApiResponseScroller[F, SlackUserId, SlackCursorId, SlackApiConversationsMembersResponse](
        initialLoader = { () => members( req ) },
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
     * https://api.slack.com/methods/conversations.open
     * return_im is set to None
     */
    def open( req: SlackApiConversationsOpenRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsOpenResponse[SlackBasicChannelInfo]]] = {

      http.post[
        SlackApiConversationsOpenRequest,
        SlackApiConversationsOpenResponse[SlackBasicChannelInfo]
      ](
        "conversations.open",
        req.copy( return_im = None ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.open
     * return_im is set to Some(true)
     */
    def openFullProfile( req: SlackApiConversationsOpenRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsOpenResponse[SlackChannelInfo]]] = {

      http.post[
        SlackApiConversationsOpenRequest,
        SlackApiConversationsOpenResponse[SlackChannelInfo]
      ](
        "conversations.open",
        req.copy( return_im = Some( true ) ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.rename
     */
    def rename( req: SlackApiConversationsRenameRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsRenameResponse]] = {

      http.post[
        SlackApiConversationsRenameRequest,
        SlackApiConversationsRenameResponse
      ](
        "conversations.rename",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.replies
     */
    def replies( req: SlackApiConversationsRepliesRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsRepliesResponse]] = {

      http.get[
        SlackApiConversationsRepliesResponse
      ](
        "conversations.replies",
        Map(
          "channel"   -> Option( req.channel.value ),
          "ts"        -> Option( req.ts.value ),
          "cursor"    -> req.cursor.map( _.value ),
          "inclusive" -> req.inclusive.map( _.toString() ),
          "latest"    -> req.latest.map( _.value ),
          "oldest"    -> req.oldest.map( _.value ),
          "limit"     -> req.limit.map( _.toString() )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.replies
     */
    def repliesScroller( req: SlackApiConversationsRepliesRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackMessage, SlackCursorId, SlackApiConversationsRepliesResponse] = {
      new SlackApiResponseScroller[F, SlackMessage, SlackCursorId, SlackApiConversationsRepliesResponse](
        initialLoader = { () => replies( req ) },
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
    def setPurpose( req: SlackApiConversationsSetPurposeRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsSetPurposeResponse]] = {

      http.post[
        SlackApiConversationsSetPurposeRequest,
        SlackApiConversationsSetPurposeResponse
      ](
        "conversations.setPurpose",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.setTopic
     */
    def setTopic( req: SlackApiConversationsSetTopicRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsSetTopicResponse]] = {

      http.post[
        SlackApiConversationsSetTopicRequest,
        SlackApiConversationsSetTopicResponse
      ](
        "conversations.setTopic",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.unarchive
     */
    def unarchive( req: SlackApiConversationsUnarchiveRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsUnarchiveResponse]] = {

      http.post[
        SlackApiConversationsUnarchiveRequest,
        SlackApiConversationsUnarchiveResponse
      ](
        "conversations.unarchive",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.mark
     */
    def unarchive( req: SlackApiConversationsMarkRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiConversationsMarkResponse]] = {

      http.post[
        SlackApiConversationsMarkRequest,
        SlackApiConversationsMarkResponse
      ](
        "conversations.unarchive",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

  }

}
