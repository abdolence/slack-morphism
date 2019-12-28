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

package org.latestbit.slack.morphism.client.models.channels

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._

import org.latestbit.slack.morphism.client.models.common.SlackDateTime
import org.latestbit.slack.morphism.client.models.messages.SlackMessage

case class SlackChannelInfo(
    id: String,
    name: String,
    created: SlackDateTime,
    creator: Option[String] = None,
    unlinked: Option[Long] = None,
    name_normalized: Option[String] = None,
    topic: Option[SlackChannelInfo.SlackTopicInfo] = None,
    purpose: Option[SlackChannelInfo.SlackPurposeInfo] = None,
    previous_names: Option[List[String]] = None,
    priority: Option[Long] = None,
    locale: Option[String] = None,
    flags: SlackChannelFlags = SlackChannelFlags(),
    lastState: SlackChannelLastState = SlackChannelLastState()
)

case class SlackChannelFlags(
    is_channel: Option[Boolean] = None,
    is_group: Option[Boolean] = None,
    is_im: Option[Boolean] = None,
    is_archived: Option[Boolean] = None,
    is_general: Option[Boolean] = None,
    is_shared: Option[Boolean] = None,
    is_org_shared: Option[Boolean] = None,
    is_member: Option[Boolean] = None,
    is_private: Option[Boolean] = None,
    is_mpim: Option[Boolean] = None,
    is_user_deleted: Option[Boolean] = None
)

case class SlackChannelLastState(
    last_read: Option[String] = None,
    latest: Option[SlackMessage] = None,
    unread_count: Option[Long] = None,
    unread_count_display: Option[Long] = None,
    members: Option[List[String]] = None
)

object SlackChannelInfo {
  case class SlackGeneralChannelInfo( value: String, creator: String, last_set: SlackDateTime )

  type SlackTopicInfo = SlackGeneralChannelInfo
  type SlackPurposeInfo = SlackGeneralChannelInfo

  implicit def slackChannelInfoEncoder()(
      implicit flagsEncoder: Encoder.AsObject[SlackChannelFlags],
      lastStateEncoder: Encoder.AsObject[SlackChannelLastState]
  ): Encoder.AsObject[SlackChannelInfo] = (model: SlackChannelInfo) => {
    JsonObject(
      "id" -> model.id.asJson,
      "name" -> model.name.asJson,
      "created" -> model.created.asJson,
      "unlinked" -> model.unlinked.asJson,
      "name_normalized" -> model.name_normalized.asJson,
      "topic" -> model.topic.asJson,
      "purpose" -> model.purpose.asJson,
      "previous_names" -> model.previous_names.asJson,
      "priority" -> model.priority.asJson,
      "locale" -> model.locale.asJson
    ).deepMerge( flagsEncoder.encodeObject( model.flags ) )
      .deepMerge( lastStateEncoder.encodeObject( model.lastState ) )
  }

  implicit val slackChannelInfoDecoder: Decoder[SlackChannelInfo] = (c: HCursor) => {
    for {
      id <- c.downField( "id" ).as[String]
      name <- c.downField( "name" ).as[String]
      created <- c.downField( "created" ).as[SlackDateTime]
      creator <- c.downField( "creator" ).as[Option[String]]
      unlinked <- c.downField( "unlinked" ).as[Option[Long]]
      name_normalized <- c.downField( "name_normalized" ).as[Option[String]]
      topic <- c.downField( "topic" ).as[Option[SlackTopicInfo]]
      purpose <- c.downField( "purpose" ).as[Option[SlackPurposeInfo]]
      previous_names <- c.downField( "previous_names" ).as[Option[List[String]]]
      priority <- c.downField( "priority" ).as[Option[Long]]
      locale <- c.downField( "locale" ).as[Option[String]]
      flags <- c.as[SlackChannelFlags]
      lastState <- c.as[SlackChannelLastState]
    } yield SlackChannelInfo(
      id,
      name,
      created,
      creator,
      unlinked,
      name_normalized,
      topic,
      purpose,
      previous_names,
      priority,
      locale,
      flags,
      lastState
    )
  }

}
