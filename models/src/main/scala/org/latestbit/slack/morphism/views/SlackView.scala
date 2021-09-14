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

package org.latestbit.slack.morphism.views

import io.circe._
import org.latestbit.circe.adt.codec._
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.messages.{ SlackBlock, SlackBlockPlainText }

/**
 * Views are app-customized visual areas within modals and Home tabs.
 * https://api.slack.com/reference/surfaces/views
 */
sealed trait SlackView

@JsonAdt( "modal" )
case class SlackModalView(
    title: SlackBlockPlainText,
    blocks: List[SlackBlock],
    close: Option[SlackBlockPlainText] = None,
    submit: Option[SlackBlockPlainText] = None,
    private_metadata: Option[String] = None,
    callback_id: Option[SlackCallbackId] = None,
    clear_on_close: Option[Boolean] = None,
    notify_on_close: Option[Boolean] = None,
    hash: Option[String] = None,
    external_id: Option[String] = None
) extends SlackView

@JsonAdt( "home" )
case class SlackHomeView(
    blocks: List[SlackBlock],
    private_metadata: Option[String] = None,
    callback_id: Option[SlackCallbackId] = None,
    external_id: Option[String] = None
) extends SlackView

case class SlackStatefulStateParams(
    id: SlackViewId,
    team_id: SlackTeamId,
    state: Option[SlackViewState] = None,
    hash: String,
    previous_view_id: Option[SlackViewId] = None,
    root_view_id: Option[SlackViewId] = None,
    app_id: Option[SlackAppId] = None,
    bot_id: Option[SlackBotId] = None
)

case class SlackStatefulView(
    stateParams: SlackStatefulStateParams,
    view: SlackView
)

case class SlackViewStateValue( `type`: String, value: String )

case class SlackViewState( values: Map[String, Json] = Map() ) {}
