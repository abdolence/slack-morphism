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
import org.latestbit.slack.morphism.client.reqresp.chat._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Chat API methods
 */
trait SlackApiChatClient extends SlackApiHttpProtocolSupport {

  import org.latestbit.slack.morphism.ext.SttpExt._

  object chat {

    /**
     * https://api.slack.com/methods/chat.delete
     */
    def delete( req: SlackApiChatDeleteRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatDeleteResponse]] = {

      http.post[SlackApiChatDeleteRequest, SlackApiChatDeleteResponse](
        "chat.delete",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.deleteScheduledMessage
     */
    def deleteScheduledMessage( req: SlackApiChatDeleteScheduledMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatDeleteScheduledMessageResponse]] = {

      http.post[
        SlackApiChatDeleteScheduledMessageRequest,
        SlackApiChatDeleteScheduledMessageResponse
      ](
        "chat.deleteScheduledMessage",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.getPermalink
     */
    def getPermalink( req: SlackApiChatGetPermalinkRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatGetPermalinkResponse]] = {

      http.get[SlackApiChatGetPermalinkResponse](
        "chat.getPermalink",
        Map(
          "channel" -> Option( req.channel ),
          "message_ts" -> Option( req.message_ts )
        )
      )
    }

    /**
     * https://api.slack.com/methods/chat.meMessage
     */
    def meMessage( req: SlackApiChatMeMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatMeMessageResponse]] = {

      http.post[SlackApiChatMeMessageRequest, SlackApiChatMeMessageResponse](
        "chat.meMessage",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.postEphemeral
     */
    def postEphemeral( req: SlackApiChatPostEphemeralRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatPostEphemeralResponse]] = {

      http.post[
        SlackApiChatPostEphemeralRequest,
        SlackApiChatPostEphemeralResponse
      ](
        "chat.postEphemeral",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.postMessage
     */
    def postMessage( req: SlackApiChatPostMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatPostMessageResponse]] = {

      http.post[SlackApiChatPostMessageRequest, SlackApiChatPostMessageResponse](
        "chat.postMessage",
        req
      )
    }

    /**
     * Post an event reply message using response_url from Slack Events
     * @param response_url a url from Slack Event
     * @param reply reply to an event
     */
    def postEventReply( response_url: String, reply: SlackApiPostEventReply )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiPostEventReplyResponse]] = {

      protectedSlackHttpApiPost[SlackApiPostEventReply, SlackApiPostEventReplyResponse](
        absoluteUri = uri"${response_url}",
        request = createSlackHttpApiRequest(),
        body = reply
      )
    }

    /**
     * https://api.slack.com/methods/chat.scheduleMessage
     */
    def scheduleMessage( req: SlackApiChatScheduleMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatScheduleMessageResponse]] = {

      http.post[
        SlackApiChatScheduleMessageRequest,
        SlackApiChatScheduleMessageResponse
      ](
        "chat.scheduleMessage",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.unfurl
     */
    def unfurl( req: SlackApiChatUnfurlRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatUnfurlResponse]] = {

      http.post[SlackApiChatUnfurlRequest, SlackApiChatUnfurlResponse](
        "chat.unfurl",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.update
     */
    def update( req: SlackApiChatUpdateRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiChatUpdateResponse]] = {

      http.post[SlackApiChatUpdateRequest, SlackApiChatUpdateResponse](
        "chat.update",
        req
      )
    }

    object scheduledMessages {

      /**
       * https://api.slack.com/methods/chat.scheduledMessages.list
       */
      def list( req: SlackApiChatScheduledMessagesListRequest )(
          implicit slackApiToken: SlackApiToken,
          ec: ExecutionContext
      ): Future[Either[SlackApiClientError, SlackApiChatScheduledMessagesListResponse]] = {

        http.post[
          SlackApiChatScheduledMessagesListRequest,
          SlackApiChatScheduledMessagesListResponse
        ](
          "chat.scheduledMessages.list",
          req
        )
      }

      /**
	     * Scrolling support for
	     * https://api.slack.com/methods/chat.scheduledMessages.list
	     */
      def listScroller( req: SlackApiChatScheduledMessagesListRequest )(
          implicit slackApiToken: SlackApiToken,
          ec: ExecutionContext
      ): SlackApiResponseScroller[SlackApiChatScheduledMessageInfo, String] = {
        new SlackApiResponseScroller[SlackApiChatScheduledMessageInfo, String](
          initialLoader = { () =>
            list( req )
          },
          batchLoader = { cursor =>
            list(
              SlackApiChatScheduledMessagesListRequest(
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
