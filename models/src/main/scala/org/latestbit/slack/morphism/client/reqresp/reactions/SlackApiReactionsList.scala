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

package org.latestbit.slack.morphism.client.reqresp.reactions

import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.messages.SlackMessage
import org.latestbit.slack.morphism.client.streaming.SlackApiScrollableResponse

/**
 * Request of https://api.slack.com/methods/reactions.list
 */
case class SlackApiReactionsListRequest(
    cursor: Option[SlackCursorId] = None,
    limit: Option[Long] = None,
    user: Option[SlackUserId] = None,
    full: Option[Boolean] = None
)

/**
 * Response of https://api.slack.com/methods/reactions.list
 */
case class SlackApiReactionsListResponse(
    override val items: List[SlackApiReactionsListItem] = List(),
    response_metadata: Option[SlackApiResponseMetadata] = None
) extends SlackApiScrollableResponse[SlackApiReactionsListItem, SlackCursorId] {

  override def getLatestPos: Option[SlackCursorId] = response_metadata.flatMap( _.next_cursor )

}

case class SlackApiReactionsListItem( channel: SlackChannelId, message: SlackMessage )
