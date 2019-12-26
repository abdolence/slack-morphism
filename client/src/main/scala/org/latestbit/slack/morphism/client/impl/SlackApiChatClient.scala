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
import org.latestbit.slack.morphism.client.models.chat._
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Chat API methods
 */
trait SlackApiChatClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  import org.latestbit.slack.morphism.ext.SttpExt._

  object chat {

    /**
     * https://api.slack.com/methods/chat.delete
     */
    def delete( req: SlackApiChatDeleteRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatDeleteResponse]] = {

      protectedSlackHttpApiPost[SlackApiChatDeleteRequest, SlackApiChatDeleteResponse](
        "chat.delete",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.deleteScheduledMessage
     */
    def deleteScheduledMessage( req: SlackApiChatDeleteScheduledMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatDeleteScheduledMessageResponse]] = {

      protectedSlackHttpApiPost[
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
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatGetPermalinkResponse]] = {

      protectedSlackHttpApiPost[SlackApiChatGetPermalinkRequest, SlackApiChatGetPermalinkResponse](
        "chat.getPermalink",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.meMessage
     */
    def meMessage( req: SlackApiChatMeMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatMeMessageResponse]] = {

      protectedSlackHttpApiPost[SlackApiChatMeMessageRequest, SlackApiChatMeMessageResponse](
        "chat.meMessage",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.postEphemeral
     */
    def postEphemeral( req: SlackApiChatPostEphemeralRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatPostEphemeralResponse]] = {

      protectedSlackHttpApiPost[
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
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatPostMessageResponse]] = {

      protectedSlackHttpApiPost[SlackApiChatPostMessageRequest, SlackApiChatPostMessageResponse](
        "chat.postMessage",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.scheduleMessage
     */
    def scheduleMessage( req: SlackApiChatScheduleMessageRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatScheduleMessageResponse]] = {

      protectedSlackHttpApiPost[
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
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatUnfurlResponse]] = {

      protectedSlackHttpApiPost[SlackApiChatUnfurlRequest, SlackApiChatUnfurlResponse](
        "chat.unfurl",
        req
      )
    }

    /**
     * https://api.slack.com/methods/chat.update
     */
    def update( req: SlackApiChatUpdateRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiChatUpdateResponse]] = {

      protectedSlackHttpApiPost[SlackApiChatUpdateRequest, SlackApiChatUpdateResponse](
        "chat.update",
        req
      )
    }

  }

}
