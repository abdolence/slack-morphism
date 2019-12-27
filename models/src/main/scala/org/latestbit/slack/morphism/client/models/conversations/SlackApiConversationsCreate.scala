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

package org.latestbit.slack.morphism.client.models.conversations

import org.latestbit.slack.morphism.client.models.channels.SlackChannelInfo

/**
 * Request of https://api.slack.com/methods/conversations.create
 */
case class SlackApiConversationsCreateRequest(
    name: String,
    is_private: Option[Boolean] = None,
    user_ds: Option[Set[String]] = None
)

/**
 * Response of https://api.slack.com/methods/conversations.create
 */
case class SlackApiConversationsCreateResponse( channel: SlackChannelInfo )
