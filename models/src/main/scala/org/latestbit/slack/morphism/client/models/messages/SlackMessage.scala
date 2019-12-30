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

package org.latestbit.slack.morphism.client.models.messages

import io.circe._
import io.circe.syntax._

import org.latestbit.circe.adt.codec._

sealed trait SlackMessage {
  val ts: String
  val channel: String
  val channel_type: Option[String]
  val hidden: Option[Boolean]
}

sealed trait SlackTextMessage {
  val text: String
  val attachments: Option[List[SlackAttachment]]
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
    override val hidden: Option[Boolean] = None,
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
    with SlackTextMessage
    with SlackPinnedMessage

@JsonAdt( "bot_message" )
case class SlackBotMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
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
    with SlackTextMessage
    with SlackPinnedMessage

@JsonAdt( "me_message" )
case class SlackMeMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
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
    with SlackTextMessage
    with SlackPinnedMessage

@JsonAdt( "message_changed" )
case class SlackMessageChanged(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessage

@JsonAdt( "message_deleted" )
case class SlackMessageDeleted(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    deleted_ts: String
) extends SlackMessage

@JsonAdt( "message_replied" )
case class SlackMessageReplied(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    message: SlackMessage
) extends SlackMessage

case class SlackMessageEdited( user: String, ts: String )

case class SlackMessageReplyInfo( user: String, ts: String )

case class SlackMessageReaction( count: Int, name: String, users: List[String] )

object SlackMessage {
  import io.circe.generic.semiauto._

  implicit val meEncoder = deriveEncoder[SlackMessageEdited]
  implicit val meDecoder = deriveDecoder[SlackMessageEdited]
  implicit val mriEncoder = deriveEncoder[SlackMessageReplyInfo]
  implicit val mriDecoder = deriveDecoder[SlackMessageReplyInfo]
  implicit val mrtEncoder = deriveEncoder[SlackMessageReaction]
  implicit val mrtDecoder = deriveDecoder[SlackMessageReaction]
  implicit val muEncoder = deriveEncoder[SlackUserMessage]
  implicit val muDecoder = deriveDecoder[SlackUserMessage]

  implicit val mcEncoder = deriveEncoder[SlackMessageChanged]
  implicit val mcDecoder = deriveDecoder[SlackMessageChanged]
  implicit val mdEncoder = deriveEncoder[SlackMessageDeleted]
  implicit val mdDecoder = deriveDecoder[SlackMessageDeleted]
  implicit val mrEncoder = deriveEncoder[SlackMessageReplied]
  implicit val mrDecoder = deriveDecoder[SlackMessageReplied]
  implicit val mbEncoder = deriveEncoder[SlackBotMessage]
  implicit val mbDecoder = deriveDecoder[SlackBotMessage]
  implicit val mmEncoder = deriveEncoder[SlackMeMessage]
  implicit val mmDecoder = deriveDecoder[SlackMeMessage]

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

  implicit val encoder: Encoder[SlackMessage] = JsonTaggedAdtCodec.createEncoderDefinition[SlackMessage](
    messageEncoderDefinition[SlackMessage]
  )

  implicit val decoder: Decoder[SlackMessage] = JsonTaggedAdtCodec.createDecoderDefinition[SlackMessage] {
    messageDecoderDefinition[SlackMessage] { cursor: HCursor =>
      cursor.as[SlackUserMessage]
    }
  }

  implicit val encoderTextMessage: Encoder[SlackTextMessage] =
    JsonTaggedAdtCodec.createEncoderDefinition[SlackTextMessage](
      messageEncoderDefinition[SlackTextMessage]
    )

  implicit val decoderTextMessage: Decoder[SlackTextMessage] =
    JsonTaggedAdtCodec.createDecoderDefinition[SlackTextMessage] {
      messageDecoderDefinition[SlackTextMessage] { cursor: HCursor =>
        cursor.as[SlackUserMessage]
      }
    }

  implicit val encoderPinnedMessage: Encoder[SlackPinnedMessage] =
    JsonTaggedAdtCodec.createEncoderDefinition[SlackPinnedMessage](
      messageEncoderDefinition[SlackPinnedMessage]
    )

  implicit val decoderPinnedMessage: Decoder[SlackPinnedMessage] =
    JsonTaggedAdtCodec.createDecoderDefinition[SlackPinnedMessage] {
      messageDecoderDefinition[SlackPinnedMessage] { cursor: HCursor =>
        cursor.as[SlackUserMessage]
      }
    }

  val TYPE_VALUE = "message"

}
