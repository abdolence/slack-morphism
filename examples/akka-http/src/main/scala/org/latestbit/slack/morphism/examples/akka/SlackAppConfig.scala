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

package org.latestbit.slack.morphism.examples.akka

/**
 * Your slack App profile data
 */
case class SlackAppConfig(
    appId: String,
    clientId: String,
    clientSecret: String,
    signingSecret: String,
    redirectUrl: Option[String] = None,
    botScope: String =
      "commands,app_mentions:read,channels:history,channels:read,dnd:read,emoji:read,im:history,im:read,im:write,mpim:history,mpim:read,mpim:write,reactions:read,reactions:write,reminders:read,reminders:write,team:read,users.profile:read,users:read,groups:history,groups:read,chat:write"
)

object SlackAppConfig {
  final val empty = SlackAppConfig( null, null, null, null )
}
