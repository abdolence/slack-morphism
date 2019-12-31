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

import org.latestbit.slack.morphism.messages._

/**
 * Events from https://api.slack.com/events
 */
sealed trait SlackEventCallbackBody

object SlackEventCallbackBody {
  import io.circe.generic.semiauto._
  import SlackMessageEvent._
  import SlackMessage._

  implicit val encoderMessageEvent: Encoder[SlackEventCallbackBody] =
    JsonTaggedAdtCodec.createEncoder[SlackEventCallbackBody]( "type" )

  implicit val decoderMessageEvent: Decoder[SlackEventCallbackBody] =
    JsonTaggedAdtCodec.createDecoder[SlackEventCallbackBody]( "type" )
}

sealed trait SlackMessageEvent extends SlackEventCallbackBody {
  val ts: String
  val channel: String
  val channel_type: Option[String]
  val hidden: Option[Boolean]
}

object SlackMessageEvent {

  import io.circe.generic.semiauto._
  import SlackMessage._
  implicit val encoderSlackMessageChanged: Encoder.AsObject[SlackMessageChanged] = deriveEncoder[SlackMessageChanged]
  implicit val decoderSlackMessageChanged: Decoder[SlackMessageChanged] = deriveDecoder[SlackMessageChanged]
  implicit val encoderSlackMessageReplied: Encoder.AsObject[SlackMessageReplied] = deriveEncoder[SlackMessageReplied]
  implicit val decoderSlackMessageReplied: Decoder[SlackMessageReplied] = deriveDecoder[SlackMessageReplied]
  implicit val encoderSlackMessageDeleted: Encoder.AsObject[SlackMessageDeleted] = deriveEncoder[SlackMessageDeleted]
  implicit val decoderSlackMessageDeleted: Decoder[SlackMessageDeleted] = deriveDecoder[SlackMessageDeleted]

  implicit val encoderMessageEvent: Encoder[SlackMessageEvent] =
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

sealed trait SlackMessage extends SlackMessageEvent {
  override val ts: String
  override val channel: String
  override val channel_type: Option[String]
  val text: String
  val attachments: Option[List[SlackAttachment]]
  val blocks: Option[List[SlackBlock]]
  val thread_ts: Option[String]
  val reactions: Option[List[SlackMessageReaction]]
  override val hidden: Option[Boolean] = None
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
    override val text: String,
    override val attachments: Option[List[SlackAttachment]] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    user: String
) extends SlackMessage
    with SlackPinnedMessage

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
    override val text: String,
    override val attachments: Option[List[SlackAttachment]] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    bot_id: String,
    username: String
) extends SlackMessage
    with SlackPinnedMessage

@JsonAdt( "me_message" )
case class SlackMeMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val thread_ts: Option[String] = None,
    override val reactions: Option[List[SlackMessageReaction]] = None,
    edited: Option[SlackMessageEdited] = None,
    override val text: String,
    override val attachments: Option[List[SlackAttachment]] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    override val permalink: Option[String] = None,
    override val pinned_to: Option[List[String]] = None,
    user: String
) extends SlackMessage
    with SlackPinnedMessage

case class SlackMessageEdited( user: String, ts: String )

case class SlackMessageReplyInfo( user: String, ts: String )

case class SlackMessageReaction( count: Int, name: String, users: List[String] )

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

  implicit val encoderPinnedMessage: Encoder[SlackPinnedMessage] =
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
