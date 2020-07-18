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
import org.latestbit.slack.morphism.client.reqresp.dnd.SlackApiDndInfoResponse
import org.latestbit.slack.morphism.client.reqresp.pins.SlackPinItem
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.messages._
import org.latestbit.slack.morphism.views.SlackView

/**
 * Events from https://api.slack.com/events
 */
sealed trait SlackEventCallbackBody

/**
 * https://api.slack.com/events/message
 */
@JsonAdt( "message" )
sealed trait SlackMessageEvent extends SlackEventCallbackBody {
  val ts: SlackTs
  val channel: Option[SlackChannelId]
  val channel_type: Option[String]
  val hidden: Option[Boolean]
}

@JsonAdt( "message_changed" )
case class SlackMessageChanged(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId],
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessageEvent

@JsonAdt( "message_deleted" )
case class SlackMessageDeleted(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    deleted_ts: SlackTs
) extends SlackMessageEvent

@JsonAdt( "message_replied" )
case class SlackMessageReplied(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessageEvent

@JsonAdt( "thread_broadcast" )
case class SlackMessageThreadBroadcast(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessageEvent

case class SlackMessageGeneralInfo(
    ts: SlackTs,
    channel: SlackChannelId,
    channel_type: Option[String],
    thread_ts: Option[SlackTs] = None
)

@JsonAdtPassThrough
sealed trait SlackMessage {
  val ts: SlackTs
  val channel: Option[SlackChannelId]
  val channel_type: Option[String]
  val text: Option[String]
  val blocks: Option[List[SlackBlock]]
  val thread_ts: Option[SlackTs]
  val reactions: Option[List[SlackMessageReaction]]
}

sealed trait SlackPinnedMessage {
  val permalink: Option[String]
  val pinned_to: Option[List[String]]
}

case class SlackUserMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[SlackTs] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    override val hidden: Option[Boolean] = None,
    user: SlackUserId
) extends SlackMessage
    with SlackPinnedMessage
    with SlackMessageEvent

@JsonAdt( "bot_message" )
case class SlackBotMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[SlackTs] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    override val hidden: Option[Boolean] = None,
    bot_id: SlackBotId,
    user: Option[String] = None
) extends SlackMessage
    with SlackPinnedMessage
    with SlackMessageEvent

@JsonAdt( "me_message" )
case class SlackMeMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[SlackTs] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    override val hidden: Option[Boolean] = None,
    user: SlackUserId
) extends SlackMessage
    with SlackPinnedMessage
    with SlackMessageEvent

@JsonAdt( "channel_join" )
case class SlackChannelJoinMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val hidden: Option[Boolean] = None,
    user: SlackUserId,
    inviter: Option[SlackUserId] = None
) extends SlackMessage
    with SlackMessageEvent {
  override val thread_ts: Option[SlackTs] = None
}

@JsonAdt( "bot_add" )
case class SlackBotAddMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val hidden: Option[Boolean] = None,
    user: SlackUserId,
    bot_id: Option[SlackBotId] = None,
    bot_link: Option[String] = None
) extends SlackMessage
    with SlackMessageEvent {
  override val thread_ts: Option[SlackTs] = None
}

@JsonAdt( "bot_remove" )
case class SlackBotRemoveMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val hidden: Option[Boolean] = None,
    user: SlackUserId,
    bot_id: Option[SlackBotId] = None,
    bot_link: Option[String] = None
) extends SlackMessage
    with SlackMessageEvent {
  override val thread_ts: Option[SlackTs] = None
}

@JsonAdt( "channel_topic" )
case class SlackChannelTopicMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: SlackUserId,
    topic: Option[String] = None
) extends SlackMessage {
  override val thread_ts: Option[SlackTs] = None
}

@JsonAdt( "channel_purpose" )
case class SlackChannelPurposeMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: SlackUserId,
    purpose: Option[String] = None
) extends SlackMessage {
  override val thread_ts: Option[SlackTs] = None
}

@JsonAdt( "channel_name" )
case class SlackChannelNameMessage(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: SlackUserId,
    old_name: Option[String] = None,
    name: String
) extends SlackMessage {
  override val thread_ts: Option[SlackTs] = None
}

case class SlackMessageEdited( user: SlackUserId, ts: SlackTs )

case class SlackMessageReplyInfo( user: SlackUserId, ts: SlackTs )

case class SlackMessageReaction( count: Int, name: String, users: List[String] )

/**
 * https://api.slack.com/events/app_home_opened
 */
@JsonAdt( "app_home_opened" )
case class SlackAppHomeOpenedEvent(
    user: SlackUserId,
    channel: SlackChannelId,
    tab: String,
    view: Option[SlackView] = None
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/app_mention
 */
@JsonAdt( "app_mention" )
case class SlackAppMentionEvent(
    override val ts: SlackTs,
    override val channel: Option[SlackChannelId] = None,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[SlackTs] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: SlackUserId,
    event_ts: SlackTs
) extends SlackEventCallbackBody
    with SlackMessage

/**
 * https://api.slack.com/events/app_uninstalled
 */
@JsonAdt( "app_uninstalled" )
case class SlackAppUninstalledEvent() extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_archive
 */
@JsonAdt( "channel_archive" )
case class SlackChannelArchiveEvent( channel: SlackChannelId, user: Option[SlackUserId] = None )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_created
 */
@JsonAdt( "channel_created" )
case class SlackChannelCreatedEvent( channel: SlackChannelInfo ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_deleted
 */
@JsonAdt( "channel_deleted" )
case class SlackChannelDeletedEvent( channel: SlackChannelId ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_history_changed
 */
@JsonAdt( "channel_history_changed" )
case class SlackChannelHistoryChangedEvent( latest: String, ts: SlackTs ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_left
 */
@JsonAdt( "channel_left" )
case class SlackChannelLeftEvent( channel: SlackChannelId ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_rename
 */
@JsonAdt( "channel_rename" )
case class SlackChannelRenameEvent( id: SlackChannelId, name: String, created: SlackDateTime )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_shared
 */
@JsonAdt( "channel_shared" )
case class SlackChannelSharedEvent( connected_team_id: SlackTeamId, channel: SlackChannelId )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_unarchive
 */
@JsonAdt( "channel_unarchive" )
case class SlackChannelUnarchiveEvent( channel: SlackChannelId, user: Option[String] = None )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_unshared
 */
@JsonAdt( "channel_unshared" )
case class SlackChannelUnsharedEvent(
    previously_connected_team_id: SlackTeamId,
    channel: SlackChannelId,
    is_ext_shared: Option[Boolean] = None
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/dnd_updated_user
 */
@JsonAdt( "dnd_updated_user" )
case class SlackDndUpdatedUserEvent( user: SlackUserId, dnd_status: SlackApiDndInfoResponse )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/emoji_changed
 */
@JsonAdt( "emoji_changed" )
case class SlackEmojiChangedEvent( subtype: Option[String] = None, names: List[String] ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_close
 */
@JsonAdt( "im_close" )
case class SlackImCloseEvent( channel: SlackChannelId, user: SlackUserId ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_created
 */
@JsonAdt( "im_created" )
case class SlackImCreatedEvent( channel: SlackChannelInfo, user: SlackUserId ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_history_changed
 */
@JsonAdt( "im_history_changed" )
case class SlackImHistoryChangedEvent( latest: String, ts: SlackTs ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_open
 */
@JsonAdt( "im_open" )
case class SlackImOpenEvent( channel: SlackChannelId, user: SlackUserId ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/member_joined_channel
 */
@JsonAdt( "member_joined_channel" )
case class SlackMemberJoinedChannelEvent(
    channel: SlackChannelId,
    user: SlackUserId,
    channel_type: Option[String] = None,
    team: SlackTeamId,
    inviter: Option[String] = None
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/member_left_channel
 */
@JsonAdt( "member_left_channel" )
case class SlackMemberLeftChannelEvent(
    channel: SlackChannelId,
    user: SlackUserId,
    channel_type: Option[String] = None,
    team: SlackTeamId
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/pin_added
 */
@JsonAdt( "pin_added" )
case class SlackPinAddedEvent( channel_id: SlackChannelId, user: SlackUserId, item: SlackPinItem )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/pin_removed
 */
@JsonAdt( "pin_removed" )
case class SlackPinRemovedEvent(
    channel_id: SlackChannelId,
    user: SlackUserId,
    item: SlackPinItem,
    has_pins: Option[Boolean] = None
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/reaction_added
 */
@JsonAdt( "reaction_added" )
case class SlackReactionAddedEvent(
    reaction: String,
    user: SlackUserId,
    item_user: SlackUserId,
    item: SlackMessageGeneralInfo
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/reaction_removed
 */
@JsonAdt( "reaction_removed" )
case class SlackReactionRemovedEvent(
    reaction: String,
    user: SlackUserId,
    item_user: SlackUserId,
    item: SlackMessageGeneralInfo
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/team_join
 */
@JsonAdt( "team_join" )
case class SlackTeamJoinEvent( user: SlackUserInfo ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/team_rename
 */
@JsonAdt( "team_rename" )
case class SlackTeamRenameEvent( name: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/tokens_revoked
 */
@JsonAdt( "tokens_revoked" )
case class SlackTokensRevokedEvent( tokens: SlackRevokedTokens ) extends SlackEventCallbackBody

case class SlackRevokedTokens( oauth: List[SlackUserId] = List(), bot: List[SlackUserId] = List() )

/**
 * https://api.slack.com/events/user_change
 */
@JsonAdt( "user_change" )
case class SlackUserChangeEvent( user: SlackUserInfo ) extends SlackEventCallbackBody
