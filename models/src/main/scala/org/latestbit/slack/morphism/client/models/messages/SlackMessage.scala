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
import io.circe.generic.auto._
import io.circe.parser._
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
}

case class SlackUserMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    override val thread_ts: Option[String] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: String,
    override val attachments: Option[List[SlackAttachment]] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: String
) extends SlackMessage
    with SlackTextMessage

@JsonAdt( "bot_message" )
case class SlackBotMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    override val thread_ts: Option[String] = None,
    edited: Option[SlackMessageEdited] = None,
    reply_count: Option[Long] = None,
    replies: Option[List[SlackMessageReplyInfo]] = None,
    override val text: String,
    override val attachments: Option[List[SlackAttachment]] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    bot_id: String,
    username: String
) extends SlackMessage
    with SlackTextMessage

@JsonAdt( "me_message" )
case class SlackMeMessage(
    override val ts: String,
    override val channel: String,
    override val channel_type: Option[String] = None,
    override val hidden: Option[Boolean] = None,
    override val thread_ts: Option[String] = None,
    edited: Option[SlackMessageEdited] = None,
    override val text: String,
    override val attachments: Option[List[SlackAttachment]] = None,
    override val blocks: Option[List[SlackBlock]] = None,
    user: String
) extends SlackMessage
    with SlackTextMessage

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

object SlackMessage {

  implicit val encoder: Encoder[SlackMessage] = JsonTaggedAdtCodec.createEncoderDefinition[SlackMessage] {
    case ( converter, obj ) =>
      // converting our case classes accordingly to obj instance type
      // and receiving JSON type field value from annotation
      val ( jsonObj, subTypeFieldAnnotationValue ) = converter.toJsonObject( obj )

      val subTypeValue: Option[String] =
        if (subTypeFieldAnnotationValue == SlackUserMessage.getClass.getSimpleName) {
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

  implicit val decoder: Decoder[SlackMessage] = JsonTaggedAdtCodec.createDecoderDefinition[SlackMessage] {
    case ( converter, cursor ) =>
      cursor.get[Option[String]]( "type" ).flatMap {

        case Some( typeFieldValue ) if typeFieldValue != TYPE_VALUE =>
          cursor.get[Option[String]]( "subtype" ).flatMap {
            case Some( subTypeValue ) => // Decode a case class from body accordingly to typeFieldValue
              converter.fromJsonObject(
                jsonTypeFieldValue = subTypeValue,
                cursor = cursor
              )
            case _ => cursor.as[SlackUserMessage]
          }

        case _ =>
          Decoder.failedWithMessage( s"Message 'type' either isn't specified in json or has an incorrect value." )(
            cursor
          )
      }
  }

  val TYPE_VALUE = "message"

}
