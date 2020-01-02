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
import org.latestbit.slack.morphism.client.reqresp.channels._
import org.latestbit.slack.morphism.messages.SlackMessage
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.common.SlackChannelInfo
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Channels API methods
 */
trait SlackApiChannelsClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object channels {

    /**
     * https://api.slack.com/methods/channels.archive
     */
    def archive( req: SlackApiChannelsArchiveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsArchiveResponse]] = {

      http.post[SlackApiChannelsArchiveRequest, SlackApiChannelsArchiveResponse](
        "channels.archive",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.create
     */
    def create( req: SlackApiChannelsCreateRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsCreateResponse]] = {

      http.post[SlackApiChannelsCreateRequest, SlackApiChannelsCreateResponse](
        "channels.create",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.history
     */
    def history( req: SlackApiChannelsHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsHistoryResponse]] = {

      http.get[SlackApiChannelsHistoryResponse](
        "channels.history",
        Map(
          "channel" -> Option( req.channel ),
          "count" -> req.count.map( _.toString() ),
          "inclusive" -> req.inclusive.map( _.toString() ),
          "latest" -> req.latest,
          "oldest" -> req.oldest,
          "unreads" -> req.unreads.map( _.toString() )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/channels.history
     */
    def historyScroller( req: SlackApiChannelsHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackMessage, String] = {
      new SlackApiResponseScroller[SlackMessage, String](
        initialLoader = { () =>
          history( req )
        },
        batchLoader = { pos =>
          history(
            SlackApiChannelsHistoryRequest(
              channel = req.channel,
              oldest = Some( pos ),
              count = req.count
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/channels.info
     */
    def info( req: SlackApiChannelsInfoRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsInfoResponse]] = {

      http.post[SlackApiChannelsInfoRequest, SlackApiChannelsInfoResponse](
        "channels.info",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.invite
     */
    def invite( req: SlackApiChannelsInviteRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsInviteResponse]] = {

      http.post[SlackApiChannelsInviteRequest, SlackApiChannelsInviteResponse](
        "channels.invite",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.join
     */
    def join( req: SlackApiChannelsJoinRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsJoinResponse]] = {

      http.post[SlackApiChannelsJoinRequest, SlackApiChannelsJoinResponse](
        "channels.join",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.kick
     */
    def kick( req: SlackApiChannelsKickRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsKickResponse]] = {

      http.post[SlackApiChannelsKickRequest, SlackApiChannelsKickResponse](
        "channels.kick",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.leave
     */
    def leave( req: SlackApiChannelsLeaveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsLeaveResponse]] = {

      http.post[SlackApiChannelsLeaveRequest, SlackApiChannelsLeaveResponse](
        "channels.leave",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.list
     */
    def list( req: SlackApiChannelsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsListResponse]] = {

      http.get[SlackApiChannelsListResponse](
        "channels.list",
        Map(
          "cursor" -> req.cursor,
          "exclude_archived" -> req.exclude_archived.map( _.toString() ),
          "exclude_members" -> req.exclude_archived.map( _.toString() ),
          "limit" -> req.limit.map( _.toString() )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/channels.list
     */
    def listScroller( req: SlackApiChannelsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackChannelInfo, String] = {
      new SlackApiResponseScroller[SlackChannelInfo, String](
        initialLoader = { () =>
          list( req )
        },
        batchLoader = { cursor =>
          list(
            SlackApiChannelsListRequest(
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/channels.mark
     */
    def mark( req: SlackApiChannelsMarkRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsMarkResponse]] = {

      http.post[SlackApiChannelsMarkRequest, SlackApiChannelsMarkResponse](
        "channels.mark",
        req
      )
    }

    /**
	   * https://api.slack.com/methods/channels.rename
	   */
    def rename( req: SlackApiChannelsRenameRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsRenameResponse]] = {

      http.post[SlackApiChannelsRenameRequest, SlackApiChannelsRenameResponse](
        "channels.rename",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.replies
     */
    def replies( req: SlackApiChannelsRepliesRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsRepliesResponse]] = {

      http.get[SlackApiChannelsRepliesResponse](
        "channels.replies",
        Map(
          "channel" -> Option( req.channel ),
          "thread_ts" -> Option( req.thread_ts )
        )
      )
    }

    /**
     * https://api.slack.com/methods/channels.setPurpose
     */
    def setPurpose( req: SlackApiChannelsSetPurposeRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsSetPurposeResponse]] = {

      http.post[
        SlackApiChannelsSetPurposeRequest,
        SlackApiChannelsSetPurposeResponse
      ](
        "channels.setPurpose",
        req
      )
    }

    /**
     * https://api.slack.com/methods/channels.setTopic
     */
    def setTopic( req: SlackApiChannelsSetTopicRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsSetTopicResponse]] = {

      http.post[SlackApiChannelsSetTopicRequest, SlackApiChannelsSetTopicResponse](
        "channels.setTopic",
        req
      )
    }

    /**
	   * https://api.slack.com/methods/channels.unarchive
	   */
    def unarchive( req: SlackApiChannelsUnarchiveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChannelsUnarchiveResponse]] = {

      http.post[
        SlackApiChannelsUnarchiveRequest,
        SlackApiChannelsUnarchiveResponse
      ](
        "channels.unarchive",
        req
      )
    }

  }

}
