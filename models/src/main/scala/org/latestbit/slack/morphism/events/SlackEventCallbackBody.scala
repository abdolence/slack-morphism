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
import io.circe.syntax._
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
@JsonAdt( SlackMessage.TYPE_VALUE )
sealed trait SlackMessageEvent extends SlackEventCallbackBody {
  val ts: String
  val channel: String
  val channel_type: Option[String]
  val hidden: Option[Boolean]
}

@JsonAdt( "message_changed" )
case class SlackMessageChanged(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessageEvent

@JsonAdt( "message_deleted" )
case class SlackMessageDeleted(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    deleted_ts: String
) extends SlackMessageEvent

@JsonAdt( "message_replied" )
case class SlackMessageReplied(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessageEvent

@JsonAdt( "thread_broadcast" )
case class SlackMessageThreadBroadcast(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessageEvent

case class SlackMessageGeneralInfo(
    ts: String,
    channel: String,
    channel_type: Option[String],
    thread_ts: Option[String] = None
)

@JsonAdtPassThrough
sealed trait SlackMessage {
  val ts: String
  val channel: String
  val channel_type: Option[String]
  val text: Option[String]
  val blocks: Option[List[SlackBlock]]
  val thread_ts: Option[String]
  val reactions: Option[List[SlackMessageReaction]]
}

sealed trait SlackPinnedMessage {
  val permalink: Option[String]
  val pinned_to: Option[List[String]]
}

case class SlackUserMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    override val hidden: Option[Boolean] = None,
    user: String
) extends SlackMessage
    with SlackPinnedMessage
    with SlackMessageEvent

@JsonAdt( "bot_message" )
case class SlackBotMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    override val hidden: Option[Boolean] = None,
    bot_id: String,
    username: String
) extends SlackMessage
    with SlackPinnedMessage
    with SlackMessageEvent

@JsonAdt( "me_message" )
case class SlackMeMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    override val hidden: Option[Boolean] = None,
    user: String
) extends SlackMessage
    with SlackPinnedMessage
    with SlackMessageEvent

case class SlackMessageEdited( user: String, ts: String )

case class SlackMessageReplyInfo( user: String, ts: String )

case class SlackMessageReaction( count: Int, name: String, users: List[String] )

/**
 * https://api.slack.com/events/app_home_opened
 */
@JsonAdt( "app_home_opened" )
case class SlackAppHomeOpenedEvent(
    user: String,
    channel: String,
    tab: String,
    event_ts: SlackDateTimeAsStr,
    view: SlackView
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/app_mention
 */
@JsonAdt( "app_mention" )
case class SlackAppMentionEvent(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: Option[String] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: String,
    event_ts: String,
    view: SlackView
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
case class SlackChannelArchiveEvent( channel: String, user: Option[String] = None ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_created
 */
@JsonAdt( "channel_created" )
case class SlackChannelCreatedEvent( channel: SlackChannelInfo ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_deleted
 */
@JsonAdt( "channel_deleted" )
case class SlackChannelDeletedEvent( channel: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_history_changed
 */
@JsonAdt( "channel_history_changed" )
case class SlackChannelHistoryChangedEvent( latest: String, ts: String, event_ts: String )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_left
 */
@JsonAdt( "channel_left" )
case class SlackChannelLeftEvent( channel: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_rename
 */
@JsonAdt( "channel_rename" )
case class SlackChannelRenameEvent( id: String, name: String, created: SlackDateTime ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_shared
 */
@JsonAdt( "channel_shared" )
case class SlackChannelSharedEvent( connected_team_id: String, channel: String, event_ts: String )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_unarchive
 */
@JsonAdt( "channel_unarchive" )
case class SlackChannelUnarchiveEvent( channel: String, user: Option[String] = None ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/channel_unshared
 */
@JsonAdt( "channel_unshared" )
case class SlackChannelUnsharedEvent(
    previously_connected_team_id: String,
    channel: String,
    event_ts: String,
    is_ext_shared: Option[Boolean] = None
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/dnd_updated_user
 */
@JsonAdt( "dnd_updated_user" )
case class SlackDndUpdatedUserEvent( user: String, dnd_status: SlackApiDndInfoResponse ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/emoji_changed
 */
@JsonAdt( "emoji_changed" )
case class SlackEmojiChangedEvent( subtype: Option[String] = None, names: List[String] ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_close
 */
@JsonAdt( "im_close" )
case class SlackImCloseEvent( channel: String, user: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_created
 */
@JsonAdt( "im_created" )
case class SlackImCreatedEvent( channel: SlackChannelInfo, user: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_history_changed
 */
@JsonAdt( "im_history_changed" )
case class SlackImHistoryChangedEvent( latest: String, ts: String, event_ts: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/im_open
 */
@JsonAdt( "im_open" )
case class SlackImOpenEvent( channel: String, user: String ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/member_joined_channel
 */
@JsonAdt( "member_joined_channel" )
case class SlackMemberJoinedChannelEvent(
    channel: String,
    user: String,
    channel_type: Option[String] = None,
    team: String,
    inviter: Option[String] = None
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/member_left_channel
 */
@JsonAdt( "member_left_channel" )
case class SlackMemberLeftChannelEvent(
    channel: String,
    user: String,
    channel_type: Option[String] = None,
    team: String
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/pin_added
 */
@JsonAdt( "pin_added" )
case class SlackPinAddedEvent( channel_id: String, user: String, item: SlackPinItem ) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/pin_removed
 */
@JsonAdt( "pin_removed" )
case class SlackPinRemovedEvent(
    channel_id: String,
    user: String,
    item: SlackPinItem,
    has_pins: Option[Boolean] = None,
    event_ts: String
) extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/reaction_added
 */
@JsonAdt( "reaction_added" )
case class SlackReactionAddedEvent( reaction: String, user: String, item_user: String, item: SlackMessageGeneralInfo )
    extends SlackEventCallbackBody

/**
 * https://api.slack.com/events/reaction_removed
 */
@JsonAdt( "reaction_removed" )
case class SlackReactionRemovedEvent(
    reaction: String,
    user: String,
    item_user: String,
    item: SlackMessageGeneralInfo,
    event_ts: String
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

case class SlackRevokedTokens( oauth: List[String] = List(), bot: List[String] = List() )

/**
 * https://api.slack.com/events/user_change
 */
@JsonAdt( "user_change" )
case class SlackUserChangeEvent( user: SlackUserInfo ) extends SlackEventCallbackBody

object SlackEventCallbackBody {
  import io.circe.generic.semiauto._

  import SlackMessage._
  import SlackChannelInfo._

  implicit val encoderSlackAppHomeOpenedEvent: Encoder.AsObject[SlackAppHomeOpenedEvent] =
    deriveEncoder[SlackAppHomeOpenedEvent]

  implicit val decoderSlackAppHomeOpenedEvent: Decoder[SlackAppHomeOpenedEvent] =
    deriveDecoder[SlackAppHomeOpenedEvent]

  implicit val encoderSlackAppMentionEvent: Encoder.AsObject[SlackAppMentionEvent] =
    deriveEncoder[SlackAppMentionEvent]

  implicit val decoderSlackAppMentionEvent: Decoder[SlackAppMentionEvent] =
    deriveDecoder[SlackAppMentionEvent]

  implicit val encoderSlackAppUninstalledEvent: Encoder.AsObject[SlackAppUninstalledEvent] =
    deriveEncoder[SlackAppUninstalledEvent]

  implicit val decoderSlackAppUninstalledEvent: Decoder[SlackAppUninstalledEvent] =
    deriveDecoder[SlackAppUninstalledEvent]

  implicit val encoderSlackChannelArchiveEvent: Encoder.AsObject[SlackChannelArchiveEvent] =
    deriveEncoder[SlackChannelArchiveEvent]

  implicit val decoderSlackChannelArchiveEvent: Decoder[SlackChannelArchiveEvent] =
    deriveDecoder[SlackChannelArchiveEvent]

  implicit val encoderSlackChannelDeletedEvent: Encoder.AsObject[SlackChannelDeletedEvent] =
    deriveEncoder[SlackChannelDeletedEvent]

  implicit val decoderSlackChannelDeletedEvent: Decoder[SlackChannelDeletedEvent] =
    deriveDecoder[SlackChannelDeletedEvent]

  implicit val encoderSlackChannelCreatedEvent: Encoder.AsObject[SlackChannelCreatedEvent] =
    deriveEncoder[SlackChannelCreatedEvent]

  implicit val decoderSlackChannelCreatedEvent: Decoder[SlackChannelCreatedEvent] =
    deriveDecoder[SlackChannelCreatedEvent]

  implicit val encoderSlackChannelHistoryChangedEvent: Encoder.AsObject[SlackChannelHistoryChangedEvent] =
    deriveEncoder[SlackChannelHistoryChangedEvent]

  implicit val decoderSlackChannelHistoryChangedEvent: Decoder[SlackChannelHistoryChangedEvent] =
    deriveDecoder[SlackChannelHistoryChangedEvent]

  implicit val encoderSlackChannelLeftEvent: Encoder.AsObject[SlackChannelLeftEvent] =
    deriveEncoder[SlackChannelLeftEvent]

  implicit val decoderSlackChannelLeftEvent: Decoder[SlackChannelLeftEvent] =
    deriveDecoder[SlackChannelLeftEvent]

  implicit val encoderSlackChannelRenameEvent: Encoder.AsObject[SlackChannelRenameEvent] =
    deriveEncoder[SlackChannelRenameEvent]

  implicit val decoderSlackChannelRenameEvent: Decoder[SlackChannelRenameEvent] =
    deriveDecoder[SlackChannelRenameEvent]

  implicit val encoderSlackChannelSharedEvent: Encoder.AsObject[SlackChannelSharedEvent] =
    deriveEncoder[SlackChannelSharedEvent]

  implicit val decoderSlackChannelSharedEvent: Decoder[SlackChannelSharedEvent] =
    deriveDecoder[SlackChannelSharedEvent]

  implicit val encoderSlackChannelUnarchiveEvent: Encoder.AsObject[SlackChannelUnarchiveEvent] =
    deriveEncoder[SlackChannelUnarchiveEvent]

  implicit val decoderSlackChannelUnarchiveEvent: Decoder[SlackChannelUnarchiveEvent] =
    deriveDecoder[SlackChannelUnarchiveEvent]

  implicit val encoderSlackChannelUnsharedEvent: Encoder.AsObject[SlackChannelUnsharedEvent] =
    deriveEncoder[SlackChannelUnsharedEvent]

  implicit val decoderSlackChannelUnsharedEvent: Decoder[SlackChannelUnsharedEvent] =
    deriveDecoder[SlackChannelUnsharedEvent]

  implicit val encoderSlackApiDndInfoResponse: Encoder.AsObject[SlackApiDndInfoResponse] =
    deriveEncoder[SlackApiDndInfoResponse]

  implicit val decoderSlackApiDndInfoResponse: Decoder[SlackApiDndInfoResponse] =
    deriveDecoder[SlackApiDndInfoResponse]

  implicit val encoderSlackDndUpdatedEvent: Encoder.AsObject[SlackDndUpdatedUserEvent] =
    deriveEncoder[SlackDndUpdatedUserEvent]

  implicit val decoderSlackDndUpdatedEvent: Decoder[SlackDndUpdatedUserEvent] =
    deriveDecoder[SlackDndUpdatedUserEvent]

  implicit val encoderSlackEmojiChangedEvent: Encoder.AsObject[SlackEmojiChangedEvent] =
    deriveEncoder[SlackEmojiChangedEvent]

  implicit val decoderSlackEmojiChangedEvent: Decoder[SlackEmojiChangedEvent] =
    deriveDecoder[SlackEmojiChangedEvent]

  implicit val encoderSlackImCloseEvent: Encoder.AsObject[SlackImCloseEvent] =
    deriveEncoder[SlackImCloseEvent]

  implicit val decoderSlackImCloseEvent: Decoder[SlackImCloseEvent] =
    deriveDecoder[SlackImCloseEvent]

  implicit val encoderSlackImHistoryChangedEvent: Encoder.AsObject[SlackImHistoryChangedEvent] =
    deriveEncoder[SlackImHistoryChangedEvent]

  implicit val decoderSlackImHistoryChangedEvent: Decoder[SlackImHistoryChangedEvent] =
    deriveDecoder[SlackImHistoryChangedEvent]

  implicit val encoderSlackImOpenEvent: Encoder.AsObject[SlackImOpenEvent] =
    deriveEncoder[SlackImOpenEvent]

  implicit val decoderSlackImOpenEvent: Decoder[SlackImOpenEvent] =
    deriveDecoder[SlackImOpenEvent]

  implicit val encoderSlackImCreatedEvent: Encoder.AsObject[SlackImCreatedEvent] =
    deriveEncoder[SlackImCreatedEvent]

  implicit val decoderSlackImCreatedEvent: Decoder[SlackImCreatedEvent] =
    deriveDecoder[SlackImCreatedEvent]

  implicit val encoderSlackMemberJoinedChannelEvent: Encoder.AsObject[SlackMemberJoinedChannelEvent] =
    deriveEncoder[SlackMemberJoinedChannelEvent]

  implicit val decoderSlackMemberJoinedChannelEvent: Decoder[SlackMemberJoinedChannelEvent] =
    deriveDecoder[SlackMemberJoinedChannelEvent]

  implicit val encoderSlackMemberLeftChannelEvent: Encoder.AsObject[SlackMemberLeftChannelEvent] =
    deriveEncoder[SlackMemberLeftChannelEvent]

  implicit val decoderSlackMemberLeftChannelEvent: Decoder[SlackMemberLeftChannelEvent] =
    deriveDecoder[SlackMemberLeftChannelEvent]

  implicit val encoderSlackPinItem: Encoder.AsObject[SlackPinItem] =
    deriveEncoder[SlackPinItem]

  implicit val decoderSlackPinItem: Decoder[SlackPinItem] =
    deriveDecoder[SlackPinItem]

  implicit val encoderSlackPinAddedEvent: Encoder.AsObject[SlackPinAddedEvent] =
    deriveEncoder[SlackPinAddedEvent]

  implicit val decoderSlackPinAddedEvent: Decoder[SlackPinAddedEvent] =
    deriveDecoder[SlackPinAddedEvent]

  implicit val encoderSlackPinRemovedEvent: Encoder.AsObject[SlackPinRemovedEvent] =
    deriveEncoder[SlackPinRemovedEvent]

  implicit val decoderSlackPinRemovedEvent: Decoder[SlackPinRemovedEvent] =
    deriveDecoder[SlackPinRemovedEvent]

  implicit val encoderSlackReactionAddedEvent: Encoder.AsObject[SlackReactionAddedEvent] =
    deriveEncoder[SlackReactionAddedEvent]

  implicit val decoderSlackReactionAddedEvent: Decoder[SlackReactionAddedEvent] =
    deriveDecoder[SlackReactionAddedEvent]

  implicit val encoderSlackReactionRemovedEvent: Encoder.AsObject[SlackReactionRemovedEvent] =
    deriveEncoder[SlackReactionRemovedEvent]

  implicit val decoderSlackReactionRemovedEvent: Decoder[SlackReactionRemovedEvent] =
    deriveDecoder[SlackReactionRemovedEvent]

  implicit val encoderSlackTeamJoinEvent: Encoder.AsObject[SlackTeamJoinEvent] =
    deriveEncoder[SlackTeamJoinEvent]

  implicit val decoderSlackTeamJoinEvent: Decoder[SlackTeamJoinEvent] =
    deriveDecoder[SlackTeamJoinEvent]

  implicit val encoderSlackTeamRenameEvent: Encoder.AsObject[SlackTeamRenameEvent] =
    deriveEncoder[SlackTeamRenameEvent]

  implicit val decoderSlackTeamRenameEvent: Decoder[SlackTeamRenameEvent] =
    deriveDecoder[SlackTeamRenameEvent]

  implicit val encoderSlackRevokedTokens: Encoder.AsObject[SlackRevokedTokens] =
    deriveEncoder[SlackRevokedTokens]

  implicit val decoderSlackRevokedTokens: Decoder[SlackRevokedTokens] =
    deriveDecoder[SlackRevokedTokens]

  implicit val encoderSlackTokensRevokedEvent: Encoder.AsObject[SlackTokensRevokedEvent] =
    deriveEncoder[SlackTokensRevokedEvent]

  implicit val decoderSlackTokensRevokedEvent: Decoder[SlackTokensRevokedEvent] =
    deriveDecoder[SlackTokensRevokedEvent]

  implicit val encoderSlackUserChangeEvent: Encoder.AsObject[SlackUserChangeEvent] =
    deriveEncoder[SlackUserChangeEvent]

  implicit val decoderSlackUserChangeEvent: Decoder[SlackUserChangeEvent] =
    deriveDecoder[SlackUserChangeEvent]

  implicit val encoderMessageEvent: Encoder[SlackEventCallbackBody] =
    JsonTaggedAdtCodec.createEncoder[SlackEventCallbackBody]( "type" )

  implicit val decoderMessageEvent: Decoder[SlackEventCallbackBody] =
    JsonTaggedAdtCodec.createDecoder[SlackEventCallbackBody]( "type" )
}

object SlackMessage {

  def messageEncoderDefinition[T]( converter: JsonTaggedAdtConverter[T], obj: T ): JsonObject = {
    // converting our case classes accordingly to obj instance type
    // and receiving JSON type field value from annotation
    val ( jsonObj, subTypeFieldAnnotationValue ) = converter.toJsonObject( obj )

    val subTypeValue: Option[String] =
      if (subTypeFieldAnnotationValue == "SlackUserMessage") {
        None
      } else
        Some( subTypeFieldAnnotationValue )

    // Our custom JSON structure
    val baseObject =
      JsonObject(
        "type" -> TYPE_VALUE.asJson,
        "subtype" -> subTypeValue.asJson
      )

    jsonObj.toMap.foldLeft( baseObject ) {
      case ( wholeObj, ( key, value ) ) =>
        wholeObj.add( key, value )
    }
  }

  def messageDecoderDefinition[T]( defaultObjectDecoder: HCursor => Decoder.Result[T] )(
      converter: JsonTaggedAdtConverter[T],
      cursor: HCursor
  ): Decoder.Result[T] = {
    cursor.get[Option[String]]( "type" ).flatMap {

      case Some( typeFieldValue ) if typeFieldValue == TYPE_VALUE =>
        cursor.get[Option[String]]( "subtype" ).flatMap {
          case Some( subTypeValue ) =>
            converter.fromJsonObject(
              jsonTypeFieldValue = subTypeValue,
              cursor = cursor
            )
          case _ => defaultObjectDecoder( cursor )
        }

      case _ =>
        Decoder.failedWithMessage( s"Message 'type' either isn't specified in json or has an incorrect value." )(
          cursor
        )
    }
  }

  import io.circe.generic.semiauto._
  implicit val encoderSlackMessageReaction: Encoder.AsObject[SlackMessageReaction] = deriveEncoder[SlackMessageReaction]
  implicit val decoderSlackMessageReaction: Decoder[SlackMessageReaction] = deriveDecoder[SlackMessageReaction]

  implicit val encoderSlackMessageReplyInfo: Encoder.AsObject[SlackMessageReplyInfo] =
    deriveEncoder[SlackMessageReplyInfo]
  implicit val decoderSlackMessageReplyInfo: Decoder[SlackMessageReplyInfo] = deriveDecoder[SlackMessageReplyInfo]
  implicit val encoderSlackMessageEdited: Encoder.AsObject[SlackMessageEdited] = deriveEncoder[SlackMessageEdited]
  implicit val decoderSlackMessageEdited: Decoder[SlackMessageEdited] = deriveDecoder[SlackMessageEdited]

  implicit val encoderSlackUserMessage: Encoder.AsObject[SlackUserMessage] = deriveEncoder[SlackUserMessage]
  implicit val decoderSlackUserMessage: Decoder[SlackUserMessage] = deriveDecoder[SlackUserMessage]
  implicit val encoderSlackMeMessage: Encoder.AsObject[SlackMeMessage] = deriveEncoder[SlackMeMessage]
  implicit val decoderSlackMeMessage: Decoder[SlackMeMessage] = deriveDecoder[SlackMeMessage]
  implicit val encoderSlackBotMessage: Encoder.AsObject[SlackBotMessage] = deriveEncoder[SlackBotMessage]
  implicit val decoderSlackBotMessage: Decoder[SlackBotMessage] = deriveDecoder[SlackBotMessage]

  implicit val encoderSlackMessageGeneralInfo: Encoder.AsObject[SlackMessageGeneralInfo] =
    deriveEncoder[SlackMessageGeneralInfo]

  implicit val decoderSlackMessageGeneralInfo: Decoder[SlackMessageGeneralInfo] =
    deriveDecoder[SlackMessageGeneralInfo]

  implicit val encoderMessage: Encoder[SlackMessage] =
    JsonTaggedAdtCodec.createEncoderDefinition[SlackMessage](
      messageEncoderDefinition[SlackMessage]
    )

  implicit val decoderMessage: Decoder[SlackMessage] =
    JsonTaggedAdtCodec.createDecoderDefinition[SlackMessage] {
      messageDecoderDefinition[SlackMessage] { cursor: HCursor =>
        cursor.as[SlackUserMessage]
      }
    }

  val TYPE_VALUE = "message"

}

object SlackPinnedMessage {

  import io.circe.generic.semiauto._
  import SlackMessage._

  implicit val encoderPinnedMessage: Encoder.AsObject[SlackPinnedMessage] =
    JsonTaggedAdtCodec.createEncoderDefinition[SlackPinnedMessage](
      SlackMessage.messageEncoderDefinition[SlackPinnedMessage]
    )

  implicit val decoderPinnedMessage: Decoder[SlackPinnedMessage] =
    JsonTaggedAdtCodec.createDecoderDefinition[SlackPinnedMessage] {
      SlackMessage.messageDecoderDefinition[SlackPinnedMessage] { cursor: HCursor =>
        cursor.as[SlackUserMessage]
      }
    }
}

object SlackMessageEvent {

  import io.circe.generic.semiauto._
  import SlackMessage._

  implicit val encoderSlackMessageChanged: Encoder.AsObject[SlackMessageChanged] =
    deriveEncoder[SlackMessageChanged]

  implicit val decoderSlackMessageChanged: Decoder[SlackMessageChanged] =
    deriveDecoder[SlackMessageChanged]

  implicit val encoderSlackMessageReplied: Encoder.AsObject[SlackMessageReplied] =
    deriveEncoder[SlackMessageReplied]

  implicit val decoderSlackMessageReplied: Decoder[SlackMessageReplied] =
    deriveDecoder[SlackMessageReplied]

  implicit val encoderSlackMessageDeleted: Encoder.AsObject[SlackMessageDeleted] =
    deriveEncoder[SlackMessageDeleted]

  implicit val decoderSlackMessageDeleted: Decoder[SlackMessageDeleted] =
    deriveDecoder[SlackMessageDeleted]

  implicit val encoderSlackMessageThreadBroadcast: Encoder.AsObject[SlackMessageThreadBroadcast] =
    deriveEncoder[SlackMessageThreadBroadcast]

  implicit val decoderSlackMessageThreadBroadcast: Decoder[SlackMessageThreadBroadcast] =
    deriveDecoder[SlackMessageThreadBroadcast]

  implicit val encoderMessageEvent: Encoder.AsObject[SlackMessageEvent] =
    JsonTaggedAdtCodec.createEncoderDefinition[SlackMessageEvent](
      SlackMessage.messageEncoderDefinition[SlackMessageEvent]
    )

  implicit val decoderMessageEvent: Decoder[SlackMessageEvent] =
    JsonTaggedAdtCodec.createDecoderDefinition[SlackMessageEvent] {
      SlackMessage.messageDecoderDefinition[SlackMessageEvent] { cursor: HCursor =>
        cursor.as[SlackUserMessage]
      }
    }
}
