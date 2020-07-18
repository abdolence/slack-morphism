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

/**
 * Slack User Info
 */
case class SlackUserInfo(
    id: SlackUserId,
    team_id: Option[SlackTeamId] = None,
    name: Option[String] = None,
    deleted: Option[Boolean] = None,
    color: Option[String] = None,
    real_name: Option[String] = None,
    tz: Option[String] = None,
    tz_label: Option[String] = None,
    tz_offset: Option[Int] = None,
    updated: Option[SlackDateTime] = None,
    locale: Option[String] = None,
    profile: Option[SlackUserProfile] = None,
    flags: SlackUserFlags = SlackUserFlags()
)

/**
 * Slack User Profile
 */
case class SlackUserProfile(
    id: Option[SlackUserId] = None,
    avatar_hash: Option[String] = None,
    status_text: Option[String] = None,
    status_expiration: Option[SlackDateTime] = None,
    real_name: Option[String] = None,
    display_name: Option[String] = None,
    real_name_normalized: Option[String] = None,
    display_name_normalized: Option[String] = None,
    email: Option[String] = None,
    icon: Option[SlackIcon] = None,
    team: Option[SlackTeamId] = None
)

/**
 * Slack User Info Flags
 * @note This class was extracted for convenience and to avoid very big class definition of [[SlackUserInfo]]
 */
case class SlackUserFlags(
    is_admin: Option[Boolean] = None,
    is_owner: Option[Boolean] = None,
    is_primary_owner: Option[Boolean] = None,
    is_restricted: Option[Boolean] = None,
    is_ultra_restricted: Option[Boolean] = None,
    is_bot: Option[Boolean] = None,
    is_stranger: Option[Boolean] = None,
    is_app_user: Option[Boolean] = None,
    has_2fa: Option[Boolean] = None
)

/**
 * Slack Basic User Info
 */
case class SlackBasicUserInfo( id: SlackUserId, team_id: Option[SlackTeamId] = None, username: Option[String] = None )
