/*
 * Copyright 2020 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

package org.latestbit.slack.morphism.events

import org.latestbit.slack.morphism.messages.{ SlackBlock, SlackMessage }

/**
 * An event reply message using response_url
 */
case class SlackApiEventMessageReply(
    text: String,
    blocks: Option[List[SlackBlock]] = None,
    response_type: Option[String] = None,
    replace_original: Option[Boolean] = None,
    delete_original: Option[Boolean] = None
)

/**
 * Response for posting event reply
 */
case class SlackApiEventMessageReplyResponse(
    channel: Option[String] = None,
    message: Option[SlackMessage] = None
)
