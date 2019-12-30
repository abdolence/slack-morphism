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

package org.latestbit.slack.morphism.client.models.oauth

import org.latestbit.slack.morphism.client.models.enterprise.SlackBasicEnterpriseInfo
import org.latestbit.slack.morphism.client.models.team.SlackTeamInfo

/**
 * Slack OAuth v2 access response
 * https://api.slack.com/methods/oauth.v2.access
 */
case class SlackOAuthV2AccessTokenResponse(
    access_token: String,
    token_type: String,
    scope: String,
    bot_user_id: Option[String] = None,
    app_id: String,
    team: SlackTeamInfo,
    enterprise: Option[SlackBasicEnterpriseInfo] = None,
    authed_user: SlackOAuthV2AuthedUser
)

/**
 * Slack OAuth v2 user info that installed a bot
 * https://api.slack.com/methods/oauth.v2.access
 */
case class SlackOAuthV2AuthedUser(
    id: String,
    scope: Option[String] = None,
    access_token: Option[String] = None,
    token_type: Option[String] = None
)
