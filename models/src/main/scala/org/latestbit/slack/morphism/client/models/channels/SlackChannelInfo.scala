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

package org.latestbit.slack.morphism.client.models.channels

import java.time.Instant

case class SlackChannelInfo(
    id: String,
    name: String,
    is_channel: Boolean = true,
    created: Instant,
    creator: String,
    is_archived: Boolean = false,
    is_general: Boolean = false,
    name_normalized: Option[String] = None,
    is_shared: Boolean = false,
    is_org_shared: Boolean = false,
    is_member: Boolean = false,
    is_private: Boolean = false,
    is_mpim: Boolean = false,
    last_read: Option[String] = None,
    latest: Option[String] = None,
    unread_count: Option[Long] = None,
    unread_count_display: Option[Long] = None,
    members: List[String] = List(),
    topic: Option[SlackChannelInfo.SlackTopicInfo] = None,
    purpose: Option[SlackChannelInfo.SlackPurposeInfo] = None,
    previous_names: List[String] = List()
)

object SlackChannelInfo {
  case class SlackGeneralChannelInfo( value: String, creator: String, last_set: Instant )

  type SlackTopicInfo = SlackGeneralChannelInfo
  type SlackPurposeInfo = SlackGeneralChannelInfo
}
