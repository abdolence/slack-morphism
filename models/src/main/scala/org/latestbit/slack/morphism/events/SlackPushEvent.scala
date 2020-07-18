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

package org.latestbit.slack.morphism.events

import org.latestbit.circe.adt.codec._
import org.latestbit.slack.morphism.common._

/**
 * Incoming Slack push event
 * https://api.slack.com/types/event
 */
sealed trait SlackPushEvent

/**
 * Push URL verification event
 * https://api.slack.com/events-api#request_url_configuration__amp__verification
 */
@JsonAdt( "url_verification" )
case class SlackUrlVerificationEvent( challenge: String ) extends SlackPushEvent

/**
 * Incoming Slack callback Event
 * https://api.slack.com/types/event
 */
@JsonAdt( "event_callback" )
case class SlackEventCallback(
    team_id: SlackTeamId,
    api_app_id: String,
    event: SlackEventCallbackBody,
    event_id: String,
    event_time: SlackDateTime,
    authed_users: Option[Seq[String]] = None
) extends SlackPushEvent

/**
 * Rate limit event
 * https://api.slack.com/events-api#rate_limiting_event
 */
@JsonAdt( "app_rate_limited" )
case class SlackAppRateLimitedEvent( team_id: SlackTeamId, minute_rate_limited: SlackDateTime, api_app_id: SlackAppId )
    extends SlackPushEvent
