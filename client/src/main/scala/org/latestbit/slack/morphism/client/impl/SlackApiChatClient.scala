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

import cats.implicits._
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.reqresp.chat._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import sttp.client3._
import org.latestbit.slack.morphism.codecs.implicits._
import org.latestbit.slack.morphism.common.SlackCursorId

/**
 * Support for Slack Chat API methods
 */
trait SlackApiChatClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object chat {

    /**
     * https://api.slack.com/methods/chat.delete
     */
    def delete( req: SlackApiChatDeleteRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatDeleteResponse]] = {

      http.post[SlackApiChatDeleteRequest, SlackApiChatDeleteResponse](
        "chat.delete",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/chat.deleteScheduledMessage
     */
    def deleteScheduledMessage( req: SlackApiChatDeleteScheduledMessageRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatDeleteScheduledMessageResponse]] = {

      http.post[
        SlackApiChatDeleteScheduledMessageRequest,
        SlackApiChatDeleteScheduledMessageResponse
      ](
        "chat.deleteScheduledMessage",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/chat.getPermalink
     */
    def getPermalink( req: SlackApiChatGetPermalinkRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatGetPermalinkResponse]] = {

      http.get[SlackApiChatGetPermalinkResponse](
        "chat.getPermalink",
        Map(
          "channel"    -> Option( req.channel.value ),
          "message_ts" -> Option( req.message_ts.value )
        )
      )
    }

    /**
     * https://api.slack.com/methods/chat.meMessage
     */
    def meMessage( req: SlackApiChatMeMessageRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatMeMessageResponse]] = {

      http.post[SlackApiChatMeMessageRequest, SlackApiChatMeMessageResponse](
        "chat.meMessage",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/chat.postEphemeral
     */
    def postEphemeral( req: SlackApiChatPostEphemeralRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatPostEphemeralResponse]] = {

      http
        .post[
          SlackApiChatPostEphemeralRequest,
          SlackApiChatPostEphemeralResponse
        ](
          "chat.postEphemeral",
          req,
          methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier4 ) ) )
        )
        .map( handleSlackEmptyRes( SlackApiChatPostEphemeralResponse() ) )
    }

    /**
     * https://api.slack.com/methods/chat.postMessage
     */
    def postMessage(
        req: SlackApiChatPostMessageRequest,
        rateControlLimit: SlackApiRateControlLimit =
          SlackApiRateControlParams.StandardLimits.Specials.PostChannelMessageLimit
    )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatPostMessageResponse]] = {

      http.post[SlackApiChatPostMessageRequest, SlackApiChatPostMessageResponse](
        "chat.postMessage",
        req,
        methodRateControl = Some(
          SlackApiMethodRateControlParams(
            specialRateLimit = Some(
              SlackApiRateControlSpecialLimit(
                key = s"chat.postMessage-${req.channel}",
                rateControlLimit
              )
            )
          )
        )
      )
    }

    /**
     * Post a webhook message using webhook url
     * @param url a url from a Slack OAuth response or from a Slack app profile configuration
     * @param req a webhook request message params
     * @param rateControlLimit optionally you may specify your own rate limit for this function
     */
    def postWebhookMessage(
        url: String,
        req: SlackApiPostWebHookRequest,
        rateControlLimit: SlackApiRateControlLimit = SlackApiRateControlParams.StandardLimits.Specials.IncomingHookLimit
    )( implicit
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiPostWebHookResponse]] = {

      sendManagedSlackHttpRequest[SlackApiPostWebHookResponse](
        request = encodePostBody( createSlackHttpApiRequest(), req ).post( uri"${url}" ),
        methodRateControl = Some(
          SlackApiMethodRateControlParams(
            specialRateLimit = Some(
              SlackApiRateControlSpecialLimit(
                key = s"webhook.post-${url}",
                rateControlLimit
              )
            )
          )
        ),
        slackApiToken = None
      ).map( handleSlackEmptyRes( SlackApiPostWebHookResponse() ) )
    }

    /**
     * https://api.slack.com/methods/chat.scheduleMessage
     */
    def scheduleMessage( req: SlackApiChatScheduleMessageRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatScheduleMessageResponse]] = {

      http.post[
        SlackApiChatScheduleMessageRequest,
        SlackApiChatScheduleMessageResponse
      ](
        "chat.scheduleMessage",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/chat.unfurl
     */
    def unfurl( req: SlackApiChatUnfurlRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatUnfurlResponse]] = {

      http.post[SlackApiChatUnfurlRequest, SlackApiChatUnfurlResponse](
        "chat.unfurl",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/chat.update
     */
    def update( req: SlackApiChatUpdateRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiChatUpdateResponse]] = {

      http.post[SlackApiChatUpdateRequest, SlackApiChatUpdateResponse](
        "chat.update",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    object scheduledMessages {

      /**
       * https://api.slack.com/methods/chat.scheduledMessages.list
       */
      def list( req: SlackApiChatScheduledMessagesListRequest )( implicit
          slackApiToken: SlackApiToken,
          backendType: SlackApiClientBackend.BackendType[F]
      ): F[Either[SlackApiClientError, SlackApiChatScheduledMessagesListResponse]] = {

        http.post[
          SlackApiChatScheduledMessagesListRequest,
          SlackApiChatScheduledMessagesListResponse
        ](
          "chat.scheduledMessages.list",
          req,
          methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
        )
      }

      /**
       * Scrolling support for
       * https://api.slack.com/methods/chat.scheduledMessages.list
       */
      def listScroller( req: SlackApiChatScheduledMessagesListRequest )( implicit
          slackApiToken: SlackApiToken,
          backendType: SlackApiClientBackend.BackendType[F]
      ): SlackApiResponseScroller[
        F,
        SlackApiChatScheduledMessageInfo,
        SlackCursorId,
        SlackApiChatScheduledMessagesListResponse
      ] = {
        new SlackApiResponseScroller[
          F,
          SlackApiChatScheduledMessageInfo,
          SlackCursorId,
          SlackApiChatScheduledMessagesListResponse
        ](
          initialLoader = { () => list( req ) },
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
