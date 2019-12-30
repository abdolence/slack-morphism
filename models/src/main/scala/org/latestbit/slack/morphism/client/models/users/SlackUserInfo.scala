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

package org.latestbit.slack.morphism.client.models.users

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._

import org.latestbit.slack.morphism.client.models.common.SlackDateTime

case class SlackUserInfo(
    id: String,
    team_id: Option[String] = None,
    name: Option[String] = None,
    deleted: Option[Boolean] = None,
    color: Option[String] = None,
    real_name: Option[String] = None,
    tz: Option[String] = None,
    tz_label: Option[String] = None,
    tz_offset: Option[Int] = None,
    updated: Option[SlackDateTime] = None,
    locale: Option[String] = None,
    profile: Option[SlackUserProfile] = None,
    flags: SlackUserFlags = SlackUserFlags()
)

case class SlackUserProfile(
    avatar_hash: Option[String] = None,
    status_text: Option[String] = None,
    status_expiration: Option[SlackDateTime] = None,
    real_name: Option[String] = None,
    display_name: Option[String] = None,
    real_name_normalized: Option[String] = None,
    display_name_normalized: Option[String] = None,
    email: Option[String] = None,
    image_original: Option[String] = None,
    image_24: Option[String] = None,
    image_32: Option[String] = None,
    image_48: Option[String] = None,
    image_72: Option[String] = None,
    image_192: Option[String] = None,
    image_512: Option[String] = None,
    team: Option[String] = None
)

case class SlackUserFlags(
    is_admin: Option[Boolean] = None,
    is_owner: Option[Boolean] = None,
    is_primary_owner: Option[Boolean] = None,
    is_restricted: Option[Boolean] = None,
    is_ultra_restricted: Option[Boolean] = None,
    is_bot: Option[Boolean] = None,
    is_stranger: Option[Boolean] = None,
    is_app_user: Option[Boolean] = None,
    has_2fa: Option[Boolean] = None
)

object SlackUserInfo {

  implicit def slackChannelInfoEncoder()(
      implicit flagsEncoder: Encoder.AsObject[SlackUserFlags]
  ): Encoder.AsObject[SlackUserInfo] = (model: SlackUserInfo) => {
    JsonObject(
      "id" -> model.id.asJson,
      "team_id" -> model.team_id.asJson,
      "name" -> model.name.asJson,
      "deleted" -> model.deleted.asJson,
      "color" -> model.color.asJson,
      "real_name" -> model.real_name.asJson,
      "tz" -> model.tz.asJson,
      "tz_label" -> model.tz_label.asJson,
      "tz_offset" -> model.tz_offset.asJson,
      "profile" -> model.profile.asJson,
      "updated" -> model.updated.asJson,
      "locale" -> model.locale.asJson
    ).deepMerge( flagsEncoder.encodeObject( model.flags ) )
  }

  implicit val slackChannelInfoDecoder: Decoder[SlackUserInfo] = (c: HCursor) => {
    for {
      id <- c.downField( "id" ).as[String]
      team_id <- c.downField( "team_id" ).as[Option[String]]
      name <- c.downField( "name" ).as[Option[String]]
      deleted <- c.downField( "deleted" ).as[Option[Boolean]]
      color <- c.downField( "color" ).as[Option[String]]
      real_name <- c.downField( "real_name" ).as[Option[String]]
      tz <- c.downField( "tz" ).as[Option[String]]
      tz_label <- c.downField( "tz_label" ).as[Option[String]]
      tz_offset <- c.downField( "tz_offset" ).as[Option[Int]]
      updated <- c.downField( "updated" ).as[Option[SlackDateTime]]
      locale <- c.downField( "locale" ).as[Option[String]]
      profile <- c.downField( "profile" ).as[Option[SlackUserProfile]]
      flags <- c.as[SlackUserFlags]
    } yield SlackUserInfo(
      id,
      team_id,
      name,
      deleted,
      color,
      real_name,
      tz,
      tz_label,
      tz_offset,
      updated,
      locale,
      profile,
      flags
    )
  }

}
