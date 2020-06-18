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

package org.latestbit.slack.morphism.common

import org.latestbit.slack.morphism.messages.SlackMessage

/**
 * A universal trait to specify either full channel info or just basic
 */
sealed trait SlackChannelProfile {
  val id: String
  val name: Option[String]
}

/**
 * Slack Channel Information
 */
case class SlackChannelInfo(
    override val id: String,
    override val name: Option[String] = None,
    created: SlackDateTime,
    creator: Option[String] = None,
    unlinked: Option[Long] = None,
    name_normalized: Option[String] = None,
    topic: Option[SlackChannelInfo.SlackTopicInfo] = None,
    purpose: Option[SlackChannelInfo.SlackPurposeInfo] = None,
    previous_names: Option[List[String]] = None,
    priority: Option[Long] = None,
    num_members: Option[Long] = None,
    locale: Option[String] = None,
    flags: SlackChannelFlags = SlackChannelFlags(),
    lastState: SlackChannelCurrentState = SlackChannelCurrentState()
) extends SlackChannelProfile

/**
 * Slack Channel Flags
 * @note This class was extracted for convenience and to avoid very big class definition of [[SlackChannelInfo]]
 */
case class SlackChannelFlags(
    is_channel: Option[Boolean] = None,
    is_group: Option[Boolean] = None,
    is_im: Option[Boolean] = None,
    is_archived: Option[Boolean] = None,
    is_general: Option[Boolean] = None,
    is_shared: Option[Boolean] = None,
    is_org_shared: Option[Boolean] = None,
    is_member: Option[Boolean] = None,
    is_private: Option[Boolean] = None,
    is_mpim: Option[Boolean] = None,
    is_user_deleted: Option[Boolean] = None
)

/**
 * Slack Channel Current State
 * @note This class was extracted for convenience and to avoid very big class definition of [[SlackChannelInfo]]
 */
case class SlackChannelCurrentState(
    last_read: Option[String] = None,
    latest: Option[SlackMessage] = None,
    unread_count: Option[Long] = None,
    unread_count_display: Option[Long] = None,
    members: Option[List[String]] = None
)

object SlackChannelInfo {

  /**
   * Auxiliary data type for channel topic and purpose
   */
  case class SlackChannelDetails(
      value: String,
      creator: Option[String] = None,
      last_set: Option[SlackDateTime] = None
  )
  type SlackTopicInfo = SlackChannelDetails
  type SlackPurposeInfo = SlackChannelDetails
}

/**
 * Basic Slack Channel information
 */
case class SlackBasicChannelInfo(
    override val id: String,
    override val name: Option[String] = None
) extends SlackChannelProfile
