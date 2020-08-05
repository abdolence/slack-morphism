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

package org.latestbit.slack.morphism.client.reqresp.chat

import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.client.streaming.SlackApiScrollableResponse

/**
 * Request of https://api.slack.com/methods/chat.scheduledMessages.list.
 */
case class SlackApiChatScheduledMessagesListRequest(
    channel: Option[SlackChannelId] = None,
    cursor: Option[SlackCursorId] = None,
    latest: Option[SlackScheduledMessageId] = None,
    limit: Option[Long] = None,
    oldest: Option[SlackScheduledMessageId] = None
)

/**
 * Response of https://api.slack.com/methods/chat.scheduledMessages.list.
 */
case class SlackApiChatScheduledMessagesListResponse(
    scheduled_messages: List[SlackApiChatScheduledMessageInfo] = List(),
    response_metadata: Option[SlackApiResponseMetadata] = None
) extends SlackApiScrollableResponse[SlackApiChatScheduledMessageInfo, SlackCursorId] {

  override def items: List[SlackApiChatScheduledMessageInfo] = scheduled_messages
  override def getLatestPos: Option[SlackCursorId]           = response_metadata.flatMap( _.next_cursor )

}

case class SlackApiChatScheduledMessageInfo(
    id: SlackScheduledMessageId,
    channel_id: SlackChannelId,
    post_at: SlackDateTime,
    date_created: SlackDateTime
)
