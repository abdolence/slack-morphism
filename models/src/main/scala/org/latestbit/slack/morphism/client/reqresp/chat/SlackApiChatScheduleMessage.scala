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

import org.latestbit.slack.morphism.common.SlackDateTimeAsStr
import org.latestbit.slack.morphism.messages.{ SlackAttachment, SlackBlock }

/**
 * Request of https://api.slack.com/methods/chat.scheduleMessage
 */
case class SlackApiChatScheduleMessageRequest(
    channel: String,
    text: String,
    post_at: SlackDateTimeAsStr,
    attachments: Option[List[SlackAttachment]] = None,
    as_user: Option[Boolean] = None,
    blocks: Option[List[SlackBlock]] = None,
    icon_emoji: Option[String] = None,
    icon_url: Option[String] = None,
    link_names: Option[Boolean] = None,
    parse: Option[String] = None,
    thread_ts: Option[String] = None,
    username: Option[String] = None,
    reply_broadcast: Option[Boolean] = None,
    unfurl_links: Option[Boolean] = None,
    unfurl_media: Option[Boolean] = None
)

/**
 * Response of https://api.slack.com/methods/chat.scheduleMessage
 */
case class SlackApiChatScheduleMessageResponse(
    channel: String,
    scheduled_message_id: String,
    post_at: SlackDateTimeAsStr
)
