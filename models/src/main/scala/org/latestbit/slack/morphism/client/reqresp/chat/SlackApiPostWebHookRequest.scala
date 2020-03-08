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

package org.latestbit.slack.morphism.client.reqresp.chat

import org.latestbit.slack.morphism.messages.SlackBlock

/**
 * A webhook message request model
 * https://api.slack.com/messaging/webhooks
 */
case class SlackApiPostWebHookRequest(
    text: String,
    blocks: Option[List[SlackBlock]] = None,
    thread_ts: Option[String] = None
)

/**
 * A webhook message response model
 * https://api.slack.com/messaging/webhooks
 */
case class SlackApiPostWebHookResponse()
