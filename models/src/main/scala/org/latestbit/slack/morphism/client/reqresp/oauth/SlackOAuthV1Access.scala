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

package org.latestbit.slack.morphism.client.reqresp.oauth

/**
 * Slack OAuth v1 access response
 * https://api.slack.com/methods/oauth.access
 */
case class SlackOAuthV1AccessTokenResponse(
    access_token: String,
    scope: String,
    team_id: String,
    enterprise_id: Option[String] = None,
    team_name: Option[String] = None,
    user_id: Option[String] = None,
    bot: Option[SlackOAuthV1BotToken] = None,
    incoming_webhook: Option[SlackOAuthIncomingWebHook] = None
)

/**
 * Slack OAuth v1 access bot token data
 * https://api.slack.com/methods/oauth.access
 */
case class SlackOAuthV1BotToken( bot_user_id: String, bot_access_token: String )
