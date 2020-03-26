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

import io.circe._
import io.circe.generic.semiauto._
import org.latestbit.circe.adt.codec._
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.messages.{ SlackBlockChoiceItem, SlackBlockText }
import org.latestbit.slack.morphism.views.{ SlackStatefulView, SlackView }

/**
 * An interaction payload is both a way of notifying your app about an interaction
 * and a bundle of information containing the where and when (and many other Ws)
 * of the interaction. It is vital to the interaction process, so your app has to be able to understand it.
 *
 * https://api.slack.com/messaging/interactivity#components
 */
sealed trait SlackInteractionEvent {
  val team: SlackBasicTeamInfo
}

/**
 * https://api.slack.com/reference/interaction-payloads/block-actions
 */
@JsonAdt( "block_actions" )
case class SlackInteractionBlockActionEvent(
    override val team: SlackBasicTeamInfo,
    user: Option[SlackBasicUserInfo] = None,
    api_app_id: String,
    container: SlackInteractionActionContainer,
    trigger_id: String,
    channel: Option[SlackBasicChannelInfo] = None,
    message: Option[SlackMessage] = None,
    view: Option[SlackView] = None,
    response_url: Option[String] = None,
    actions: Option[List[SlackInteractionActionInfo]] = None
) extends SlackInteractionEvent

@JsonAdt( "dialog_submission" )
case class SlackInteractionDialogueSubmissionEvent(
    override val team: SlackBasicTeamInfo,
    user: SlackBasicUserInfo,
    channel: Option[SlackBasicChannelInfo] = None,
    callback_id: Option[String] = None,
    state: Option[String] = None,
    submission: Map[String, String]
) extends SlackInteractionEvent

/**
 * https://api.slack.com/reference/interaction-payloads/actions
 */
@JsonAdt( "message_action" )
case class SlackInteractionMessageActionEvent(
    override val team: SlackBasicTeamInfo,
    user: SlackBasicUserInfo,
    channel: Option[SlackBasicChannelInfo] = None,
    message: Option[SlackMessage] = None,
    callback_id: String,
    trigger_id: String,
    response_url: String,
    actions: Option[List[SlackInteractionActionInfo]] = None
) extends SlackInteractionEvent

/**
 * https://api.slack.com/reference/interaction-payloads/shortcuts
 */
@JsonAdt( "shortcut" )
case class SlackInteractionShortcutEvent(
    override val team: SlackBasicTeamInfo,
    user: SlackBasicUserInfo,
    callback_id: String,
    trigger_id: String,
    actions: Option[List[SlackInteractionActionInfo]] = None
) extends SlackInteractionEvent

/**
 * https://api.slack.com/reference/interaction-payloads/views
 */
@JsonAdt( "view_submission" )
case class SlackInteractionViewSubmissionEvent(
    team: SlackBasicTeamInfo,
    user: SlackBasicUserInfo,
    view: SlackStatefulView
) extends SlackInteractionEvent

/**
 * https://api.slack.com/reference/interaction-payloads/views
 */
@JsonAdt( "view_closed" )
case class SlackInteractionViewClosedEvent(
    team: SlackBasicTeamInfo,
    user: SlackBasicUserInfo,
    view: SlackStatefulView
) extends SlackInteractionEvent

sealed trait SlackInteractionActionContainer

@JsonAdt( "message" )
case class SlackInteractionActionMessageContainer(
    message_ts: String,
    channel_id: Option[String] = None,
    is_ephemeral: Option[Boolean] = None,
    is_app_unfurl: Option[Boolean] = None
) extends SlackInteractionActionContainer

@JsonAdt( "view" )
case class SlackInteractionActionViewContainer(
    view_id: String
) extends SlackInteractionActionContainer

case class SlackInteractionActionInfo(
    `type`: String,
    action_id: String,
    block_id: Option[String] = None,
    text: Option[SlackBlockText] = None,
    value: Option[String] = None,
    selected_option: Option[SlackBlockChoiceItem[SlackBlockText]] = None,
    action_ts: Option[String] = None
)
