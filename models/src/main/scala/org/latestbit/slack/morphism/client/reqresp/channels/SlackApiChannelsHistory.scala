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

package org.latestbit.slack.morphism.client.reqresp.channels

import org.latestbit.slack.morphism.client.streaming.SlackApiScrollableResponse
import org.latestbit.slack.morphism.messages.SlackTextMessage

/**
 * Request of https://api.slack.com/methods/channels.history
 */
case class SlackApiChannelsHistoryRequest(
    channel: String,
    count: Option[Int] = None,
    inclusive: Option[Boolean] = None,
    latest: Option[String] = None,
    oldest: Option[String] = None,
    unreads: Option[Boolean] = None
)

/**
 * Response of https://api.slack.com/methods/channels.history
 */
case class SlackApiChannelsHistoryResponse(
    messages: List[SlackTextMessage],
    has_more: Option[Boolean] = None,
    latest: Option[String] = None
) extends SlackApiScrollableResponse[SlackTextMessage, String] {

  override def items: List[SlackTextMessage] = messages
  override def getLatestPos: Option[String] = latest

}
