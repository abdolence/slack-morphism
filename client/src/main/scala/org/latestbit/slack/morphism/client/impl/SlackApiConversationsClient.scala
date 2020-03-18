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
import org.latestbit.slack.morphism.client.ratectrl.SlackApiRateControlParams
import org.latestbit.slack.morphism.client.reqresp.conversations._
import org.latestbit.slack.morphism.messages.SlackMessage
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.common.SlackChannelInfo
import sttp.client._
import org.latestbit.slack.morphism.codecs.implicits._

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
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsArchiveResponse]] = {

      http.post[
        SlackApiConversationsArchiveRequest,
        SlackApiConversationsArchiveResponse
      ](
        "conversations.archive",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.close
     */
    def close( req: SlackApiConversationsCloseRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsCloseResponse]] = {

      http.post[
        SlackApiConversationsCloseRequest,
        SlackApiConversationsCloseResponse
      ](
        "conversations.close",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.create
     */
    def create( req: SlackApiConversationsCreateRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsCreateResponse]] = {

      http.post[
        SlackApiConversationsCreateRequest,
        SlackApiConversationsCreateResponse
      ](
        "conversations.create",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.history
     */
    def history( req: SlackApiConversationsHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsHistoryResponse]] = {

      http.get[
        SlackApiConversationsHistoryResponse
      ](
        "conversations.history",
        Map(
          "channel" -> Option( req.channel ),
          "cursor" -> req.cursor,
          "inclusive" -> req.inclusive.map( _.toString() ),
          "latest" -> req.latest,
          "limit" -> req.latest.map( _.toString() ),
          "oldest" -> req.oldest
        ),
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.history
     */
    def historyScroller( req: SlackApiConversationsHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackMessage, String] = {
      new SlackApiResponseScroller[SlackMessage, String](
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
    def info( req: SlackApiConversationsInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsInfoResponse]] = {

      http.get[
        SlackApiConversationsInfoResponse
      ](
        "conversations.info",
        Map(
          "channel" -> Option( req.channel ),
          "include_locale" -> req.include_locale.map( _.toString() ),
          "include_num_members" -> req.include_num_members.map( _.toString() )
        ),
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.invite
     */
    def invite( req: SlackApiConversationsInviteRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsInviteResponse]] = {

      http.post[
        SlackApiConversationsInviteRequest,
        SlackApiConversationsInviteResponse
      ](
        "conversations.invite",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.join
     */
    def join( req: SlackApiConversationsJoinRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsJoinResponse]] = {

      http.post[
        SlackApiConversationsJoinRequest,
        SlackApiConversationsJoinResponse
      ](
        "conversations.join",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.kick
     */
    def kick( req: SlackApiConversationsKickRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsKickResponse]] = {

      http.post[
        SlackApiConversationsKickRequest,
        SlackApiConversationsKickResponse
      ](
        "conversations.kick",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.leave
     */
    def leave( req: SlackApiConversationsLeaveRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsLeaveResponse]] = {

      http.post[
        SlackApiConversationsLeaveRequest,
        SlackApiConversationsLeaveResponse
      ](
        "conversations.leave",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.list
     */
    def list( req: SlackApiConversationsListRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsListResponse]] = {

      http.get[
        SlackApiConversationsListResponse
      ](
        "conversations.list",
        Map(
          "cursor" -> req.cursor,
          "exclude_archived" -> req.exclude_archived.map( _.toString() ),
          "limit" -> req.limit.map( _.toString() ),
          "types" -> req.types.map( _.mkString( "," ) )
        ),
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.list
     */
    def listScroller( req: SlackApiConversationsListRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackChannelInfo, String] = {
      new SlackApiResponseScroller[SlackChannelInfo, String](
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
    def members( req: SlackApiConversationsMembersRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsMembersResponse]] = {

      http.get[
        SlackApiConversationsMembersResponse
      ](
        "conversations.members",
        Map(
          "channel" -> Option( req.channel ),
          "cursor" -> req.cursor,
          "limit" -> req.limit.map( _.toString() )
        ),
        methodTierLevel = Some( SlackApiRateControlParams.TIER_4 )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.members
     */
    def membersScroller( req: SlackApiConversationsMembersRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): SlackApiResponseScroller[String, String] = {
      new SlackApiResponseScroller[String, String](
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
	   * https://api.slack.com/methods/conversations.rename
	   */
    def rename( req: SlackApiConversationsRenameRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsRenameResponse]] = {

      http.post[
        SlackApiConversationsRenameRequest,
        SlackApiConversationsRenameResponse
      ](
        "conversations.rename",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.replies
     */
    def replies( req: SlackApiConversationsRepliesRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsRepliesResponse]] = {

      http.get[
        SlackApiConversationsRepliesResponse
      ](
        "conversations.replies",
        Map(
          "channel" -> Option( req.channel ),
          "ts" -> Option( req.ts ),
          "cursor" -> req.cursor,
          "inclusive" -> req.inclusive.map( _.toString() ),
          "latest" -> req.latest,
          "oldest" -> req.oldest,
          "limit" -> req.limit.map( _.toString() )
        ),
        methodTierLevel = Some( SlackApiRateControlParams.TIER_3 )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/conversations.replies
     */
    def repliesScroller( req: SlackApiConversationsRepliesRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackMessage, String] = {
      new SlackApiResponseScroller[SlackMessage, String](
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
    def setPurpose( req: SlackApiConversationsSetPurposeRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsSetPurposeResponse]] = {

      http.post[
        SlackApiConversationsSetPurposeRequest,
        SlackApiConversationsSetPurposeResponse
      ](
        "conversations.setPurpose",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
     * https://api.slack.com/methods/conversations.setTopic
     */
    def setTopic( req: SlackApiConversationsSetTopicRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsSetTopicResponse]] = {

      http.post[
        SlackApiConversationsSetTopicRequest,
        SlackApiConversationsSetTopicResponse
      ](
        "conversations.setTopic",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

    /**
	   * https://api.slack.com/methods/conversations.unarchive
	   */
    def unarchive( req: SlackApiConversationsUnarchiveRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiConversationsUnarchiveResponse]] = {

      http.post[
        SlackApiConversationsUnarchiveRequest,
        SlackApiConversationsUnarchiveResponse
      ](
        "conversations.unarchive",
        req,
        methodTierLevel = Some( SlackApiRateControlParams.TIER_2 )
      )
    }

  }

}
