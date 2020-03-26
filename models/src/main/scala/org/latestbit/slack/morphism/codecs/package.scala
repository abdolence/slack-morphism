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

package org.latestbit.slack.morphism

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.latestbit.slack.morphism.common.SlackChannelInfo._
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.messages._
import org.latestbit.circe.adt.codec._
import org.latestbit.slack.morphism.client.reqresp.dnd.SlackApiDndInfoResponse
import org.latestbit.slack.morphism.client.reqresp.pins.SlackPinItem
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.views._
import org.latestbit.slack.morphism.client.reqresp.apps._
import org.latestbit.slack.morphism.client.reqresp.auth._
import org.latestbit.slack.morphism.client.reqresp.bots._
import org.latestbit.slack.morphism.client.reqresp.channels._
import org.latestbit.slack.morphism.client.reqresp.chat._
import org.latestbit.slack.morphism.client.reqresp.conversations._
import org.latestbit.slack.morphism.client.reqresp.dnd._
import org.latestbit.slack.morphism.client.reqresp.emoji._
import org.latestbit.slack.morphism.client.reqresp.im._
import org.latestbit.slack.morphism.client.reqresp.interaction._
import org.latestbit.slack.morphism.client.reqresp.internal._
import org.latestbit.slack.morphism.client.reqresp.oauth._
import org.latestbit.slack.morphism.client.reqresp.pins._
import org.latestbit.slack.morphism.client.reqresp.reactions._
import org.latestbit.slack.morphism.client.reqresp.team._
import org.latestbit.slack.morphism.client.reqresp.test._
import org.latestbit.slack.morphism.client.reqresp.users._
import org.latestbit.slack.morphism.client.reqresp.views._

/**
 * This package contains implicit circe JSON encoders and decoders
 * for all of the events and request/response models
 */
package object codecs {

  trait CirceCodecs {

    implicit val encoderSlackGeneralResponseParams: Encoder.AsObject[SlackGeneralResponseParams] =
      deriveEncoder[SlackGeneralResponseParams]

    implicit val decoderSlackGeneralResponseParams: Decoder[SlackGeneralResponseParams] =
      deriveDecoder[SlackGeneralResponseParams]

    implicit val encoderSlackApiResponseMetadata: Encoder.AsObject[SlackApiResponseMetadata] =
      deriveEncoder[SlackApiResponseMetadata]

    implicit val decoderSlackApiResponseMetadata: Decoder[SlackApiResponseMetadata] =
      deriveDecoder[SlackApiResponseMetadata]

    implicit val encoderSlackBasicTeamInfo: Encoder.AsObject[SlackBasicTeamInfo] = deriveEncoder[SlackBasicTeamInfo]
    implicit val decoderSlackBasicTeamInfo: Decoder[SlackBasicTeamInfo] = deriveDecoder[SlackBasicTeamInfo]

    implicit val encoderSlackBasicEnterpriseInfo: Encoder.AsObject[SlackBasicEnterpriseInfo] =
      deriveEncoder[SlackBasicEnterpriseInfo]

    implicit val decoderSlackBasicEnterpriseInfo: Decoder[SlackBasicEnterpriseInfo] =
      deriveDecoder[SlackBasicEnterpriseInfo]

    implicit val encoderSlackTeamProfileField: Encoder.AsObject[SlackTeamProfileField] =
      deriveEncoder[SlackTeamProfileField]
    implicit val decoderSlackTeamProfileField: Decoder[SlackTeamProfileField] = deriveDecoder[SlackTeamProfileField]

    implicit val encoderSlackTeamProfile: Encoder.AsObject[SlackTeamProfile] = deriveEncoder[SlackTeamProfile]
    implicit val decoderSlackTeamProfile: Decoder[SlackTeamProfile] = deriveDecoder[SlackTeamProfile]

    implicit val encoderSlackTeamInfo: Encoder.AsObject[SlackTeamInfo] = deriveEncoder[SlackTeamInfo]
    implicit val decoderSlackTeamInfo: Decoder[SlackTeamInfo] = deriveDecoder[SlackTeamInfo]

    implicit val encoderSlackBasicChannelInfo: Encoder.AsObject[SlackBasicChannelInfo] =
      deriveEncoder[SlackBasicChannelInfo]
    implicit val decoderSlackBasicChannelInfo: Decoder[SlackBasicChannelInfo] = deriveDecoder[SlackBasicChannelInfo]

    implicit val encoderSlackChannelDetails: Encoder.AsObject[SlackChannelDetails] =
      deriveEncoder[SlackChannelDetails]

    implicit val decoderSlackChannelDetails: Decoder[SlackChannelDetails] =
      deriveDecoder[SlackChannelDetails]

    implicit val encoderSlackChannelFlags: Encoder.AsObject[SlackChannelFlags] = deriveEncoder[SlackChannelFlags]
    implicit val decoderSlackChannelFlags: Decoder[SlackChannelFlags] = deriveDecoder[SlackChannelFlags]

    implicit val encoderSlackChannelLastState: Encoder.AsObject[SlackChannelCurrentState] =
      deriveEncoder[SlackChannelCurrentState]

    implicit val decoderSlackChannelLastState: Decoder[SlackChannelCurrentState] =
      deriveDecoder[SlackChannelCurrentState]

    def createSlackChannelInfoEncoder()(
        implicit flagsEncoder: Encoder.AsObject[SlackChannelFlags],
        lastStateEncoder: Encoder.AsObject[SlackChannelCurrentState]
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
        "num_members" -> model.num_members.asJson,
        "locale" -> model.locale.asJson
      ).deepMerge( flagsEncoder.encodeObject( model.flags ) )
        .deepMerge( lastStateEncoder.encodeObject( model.lastState ) )
    }

    implicit val slackChannelInfoEncoder = createSlackChannelInfoEncoder()

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
        num_members <- c.downField( "num_members" ).as[Option[Long]]
        locale <- c.downField( "locale" ).as[Option[String]]
        flags <- c.as[SlackChannelFlags]
        lastState <- c.as[SlackChannelCurrentState]
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
        num_members,
        locale,
        flags,
        lastState
      )
    }

    def createUserProfileEncoder(): Encoder.AsObject[SlackUserProfile] = (model: SlackUserProfile) => {
      implicit val encoderSlackUserProfile: Encoder.AsObject[SlackUserProfile] = deriveEncoder[SlackUserProfile]

      model.icon
        .map { icon =>
          encoderSlackUserProfile.encodeObject( model.copy( icon = None ) ).deepMerge( icon.asJsonObject )
        }
        .getOrElse(
          encoderSlackUserProfile.encodeObject( model )
        )
    }

    def createDecoderUserProfile(): Decoder[SlackUserProfile] = (cursor: HCursor) => {
      for {
        icon <- cursor.as[SlackIcon]
        baseUserInfo <- cursor.as[SlackUserProfile]( deriveDecoder[SlackUserProfile] )
      } yield baseUserInfo.copy( icon = Option( icon ) )
    }

    implicit val encoderUserProfile: Encoder.AsObject[SlackUserProfile] = createUserProfileEncoder()
    implicit val decoderUserProfile: Decoder[SlackUserProfile] = createDecoderUserProfile()

    implicit val encoderSlackUserFlags: Encoder.AsObject[SlackUserFlags] = deriveEncoder[SlackUserFlags]
    implicit val decoderSlackUserFlags: Decoder[SlackUserFlags] = deriveDecoder[SlackUserFlags]

    def createSlackUserInfoEncoder(): Encoder.AsObject[SlackUserInfo] = (model: SlackUserInfo) => {
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
      ).deepMerge( encoderSlackUserFlags.encodeObject( model.flags ) )
    }

    def createSlackUserInfoDecoder(): Decoder[SlackUserInfo] = (c: HCursor) => {
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

    implicit val encoderUserInfo = createSlackUserInfoEncoder()
    implicit val decoderUserInfo = createSlackUserInfoDecoder()

    implicit val encoderSlackUserBasicInfo: Encoder.AsObject[SlackBasicUserInfo] = deriveEncoder[SlackBasicUserInfo]
    implicit val decoderSlackUserBasicInfo: Decoder[SlackBasicUserInfo] = deriveDecoder[SlackBasicUserInfo]

    def messageEncoderDefinition[T]( converter: JsonTaggedAdtEncoder[T], obj: T ): JsonObject = {
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
          "type" -> SLACK_TYPE_VALUE.asJson,
          "subtype" -> subTypeValue.asJson
        )

      jsonObj.toMap.foldLeft( baseObject ) {
        case ( wholeObj, ( key, value ) ) =>
          wholeObj.add( key, value )
      }
    }

    def messageDecoderDefinition[T]( defaultObjectDecoder: HCursor => Decoder.Result[T] )(
        converter: JsonTaggedAdtDecoder[T],
        cursor: HCursor
    ): Decoder.Result[T] = {
      cursor.get[Option[String]]( "type" ).flatMap {

        case Some( typeFieldValue ) if typeFieldValue == SLACK_TYPE_VALUE =>
          cursor.get[Option[String]]( "subtype" ).flatMap {
            case Some( subTypeValue ) =>
              converter.fromJsonObject(
                jsonTypeFieldValue = subTypeValue,
                cursor = cursor
              )
            case _ => {
              cursor.get[Option[String]]( "bot_id" ).flatMap {
                case Some( _ ) => {
                  converter.fromJsonObject(
                    jsonTypeFieldValue = "bot_message",
                    cursor = cursor
                  )
                }
                case _ => defaultObjectDecoder( cursor )
              }
            }

          }

        case _ =>
          Decoder.failedWithMessage( s"Message 'type' either isn't specified in json or has an incorrect value." )(
            cursor
          )
      }
    }

    implicit val encoderSlackSectionBlockElement: Encoder.AsObject[SlackSectionBlockElement] =
      JsonTaggedAdtCodec.createEncoder[SlackSectionBlockElement]( "type" )

    implicit val decoderSlackSectionBlockElement: Decoder[SlackSectionBlockElement] =
      JsonTaggedAdtCodec.createDecoder[SlackSectionBlockElement]( "type" )

    implicit val encoderSlackContextBlockElement: Encoder.AsObject[SlackContextBlockElement] =
      JsonTaggedAdtCodec.createEncoder[SlackContextBlockElement]( "type" )

    implicit val decoderSlackContextBlockElement: Decoder[SlackContextBlockElement] =
      JsonTaggedAdtCodec.createDecoder[SlackContextBlockElement]( "type" )

    implicit val encoderSlackActionBlockElement: Encoder.AsObject[SlackActionBlockElement] =
      JsonTaggedAdtCodec.createEncoder[SlackActionBlockElement]( "type" )

    implicit val decoderSlackActionBlockElement: Decoder[SlackActionBlockElement] =
      JsonTaggedAdtCodec.createDecoder[SlackActionBlockElement]( "type" )

    implicit val encoderSlackInputBlockElement: Encoder.AsObject[SlackInputBlockElement] =
      JsonTaggedAdtCodec.createEncoder[SlackInputBlockElement]( "type" )

    implicit val decoderSlackInputBlockElement: Decoder[SlackInputBlockElement] =
      JsonTaggedAdtCodec.createDecoder[SlackInputBlockElement]( "type" )

    implicit val encoderSlackRichBlockElement: Encoder.AsObject[SlackRichBlockElement] =
      JsonTaggedAdtCodec.createEncoder[SlackRichBlockElement]( "type" )

    implicit val decoderSlackRichBlockElement: Decoder[SlackRichBlockElement] =
      JsonTaggedAdtCodec.createDecoder[SlackRichBlockElement]( "type" )

    implicit val encoderSlackInputBlock: Encoder.AsObject[SlackInputBlock] = deriveEncoder[SlackInputBlock]
    implicit val decoderSlackInputBlock: Decoder[SlackInputBlock] = deriveDecoder[SlackInputBlock]
    implicit val encoderSlackSectionBlock: Encoder.AsObject[SlackSectionBlock] = deriveEncoder[SlackSectionBlock]
    implicit val decoderSlackSectionBlock: Decoder[SlackSectionBlock] = deriveDecoder[SlackSectionBlock]
    implicit val encoderSlackRichTextBlock: Encoder.AsObject[SlackRichTextBlock] = deriveEncoder[SlackRichTextBlock]
    implicit val decoderSlackRichTextBlock: Decoder[SlackRichTextBlock] = deriveDecoder[SlackRichTextBlock]
    implicit val encoderSlackImageBlock: Encoder.AsObject[SlackImageBlock] = deriveEncoder[SlackImageBlock]
    implicit val decoderSlackImageBlock: Decoder[SlackImageBlock] = deriveDecoder[SlackImageBlock]
    implicit val encoderSlackDividerBlock: Encoder.AsObject[SlackDividerBlock] = deriveEncoder[SlackDividerBlock]
    implicit val decoderSlackDividerBlock: Decoder[SlackDividerBlock] = deriveDecoder[SlackDividerBlock]
    implicit val encoderSlackContextBlock: Encoder.AsObject[SlackContextBlock] = deriveEncoder[SlackContextBlock]
    implicit val decoderSlackContextBlock: Decoder[SlackContextBlock] = deriveDecoder[SlackContextBlock]
    implicit val encoderSlackFileBlock: Encoder.AsObject[SlackFileBlock] = deriveEncoder[SlackFileBlock]
    implicit val decoderSlackFileBlock: Decoder[SlackFileBlock] = deriveDecoder[SlackFileBlock]
    implicit val encoderSlackActionsBlock: Encoder.AsObject[SlackActionsBlock] = deriveEncoder[SlackActionsBlock]
    implicit val decoderSlackActionsBlock: Decoder[SlackActionsBlock] = deriveDecoder[SlackActionsBlock]

    implicit val encoderSlackBlock: Encoder[SlackBlock] = JsonTaggedAdtCodec.createEncoder[SlackBlock]( "type" )
    implicit val decoderSlackBlock: Decoder[SlackBlock] = JsonTaggedAdtCodec.createDecoder[SlackBlock]( "type" )

    implicit val encoderSlackBlockPlainText: Encoder.AsObject[SlackBlockPlainText] =
      JsonTaggedAdtCodec.createEncoder[SlackBlockPlainText]( "type" )

    implicit val encoderSlackMarkDownText: Encoder.AsObject[SlackBlockMarkDownText] =
      JsonTaggedAdtCodec.createEncoder[SlackBlockMarkDownText]( "type" )

    implicit val decoderSlackBlockPlainText: Decoder[SlackBlockPlainText] =
      JsonTaggedAdtCodec.createDecoder[SlackBlockPlainText]( "type" )

    implicit val decoderSlackMarkDownText: Decoder[SlackBlockMarkDownText] =
      JsonTaggedAdtCodec.createDecoder[SlackBlockMarkDownText]( "type" )

    implicit val encoderSlackBlockText: Encoder.AsObject[SlackBlockText] =
      JsonTaggedAdtCodec.createEncoder[SlackBlockText]( "type" )

    implicit val decoderSlackBlockText: Decoder[SlackBlockText] =
      JsonTaggedAdtCodec.createDecoder[SlackBlockText]( "type" )

    implicit val encoderSlackBlockConfirmItem: Encoder.AsObject[SlackBlockConfirmItem] =
      deriveEncoder[SlackBlockConfirmItem]
    implicit val decoderSlackBlockConfirmItem: Decoder[SlackBlockConfirmItem] = deriveDecoder[SlackBlockConfirmItem]

    implicit val encoderSlackBlockButtonElement: Encoder.AsObject[SlackBlockButtonElement] =
      deriveEncoder[SlackBlockButtonElement]

    implicit val decoderSlackBlockButtonElement: Decoder[SlackBlockButtonElement] =
      deriveDecoder[SlackBlockButtonElement]

    implicit val encoderSlackBlockRichTextList: Encoder.AsObject[SlackBlockRichTextList] =
      deriveEncoder[SlackBlockRichTextList]
    implicit val decoderSlackBlockRichTextList: Decoder[SlackBlockRichTextList] = deriveDecoder[SlackBlockRichTextList]

    implicit val encoderSlackBlockOptionItemBlockText: Encoder.AsObject[SlackBlockChoiceItem[SlackBlockText]] =
      deriveEncoder[SlackBlockChoiceItem[SlackBlockText]]

    implicit val decoderSlackBlockOptionItemBlockText: Decoder[SlackBlockChoiceItem[SlackBlockText]] =
      deriveDecoder[SlackBlockChoiceItem[SlackBlockText]]

    implicit val encoderSlackBlockOptionGroupBlockText: Encoder.AsObject[SlackBlockOptionGroup[SlackBlockText]] =
      deriveEncoder[SlackBlockOptionGroup[SlackBlockText]]

    implicit val decoderSlackBlockOptionGroupBlockText: Decoder[SlackBlockOptionGroup[SlackBlockText]] =
      deriveDecoder[SlackBlockOptionGroup[SlackBlockText]]

    implicit val encoderSlackBlockOptionItemBlockPlainText
        : Encoder.AsObject[SlackBlockChoiceItem[SlackBlockPlainText]] =
      deriveEncoder[SlackBlockChoiceItem[SlackBlockPlainText]]

    implicit val decoderSlackBlockOptionItemBlockPlainText: Decoder[SlackBlockChoiceItem[SlackBlockPlainText]] =
      deriveDecoder[SlackBlockChoiceItem[SlackBlockPlainText]]

    implicit val encoderSlackBlockOptionGroupBlockPlainText
        : Encoder.AsObject[SlackBlockOptionGroup[SlackBlockPlainText]] =
      deriveEncoder[SlackBlockOptionGroup[SlackBlockPlainText]]

    implicit val decoderSlackBlockOptionGroupBlockPlainText: Decoder[SlackBlockOptionGroup[SlackBlockPlainText]] =
      deriveDecoder[SlackBlockOptionGroup[SlackBlockPlainText]]

    implicit val encoderSlackBlockSelectElement: Encoder.AsObject[SlackBlockSelectElement] =
      deriveEncoder[SlackBlockSelectElement]

    implicit val decoderSlackBlockSelectElement: Decoder[SlackBlockSelectElement] =
      deriveDecoder[SlackBlockSelectElement]

    implicit val encoderSlackBlockImageElement: Encoder.AsObject[SlackBlockImageElement] =
      deriveEncoder[SlackBlockImageElement]
    implicit val decoderSlackBlockImageElement: Decoder[SlackBlockImageElement] = deriveDecoder[SlackBlockImageElement]

    implicit val encoderSlackBlockStaticSelectElement: Encoder.AsObject[SlackBlockStaticSelectElement] =
      deriveEncoder[SlackBlockStaticSelectElement]

    implicit val decoderSlackBlockStaticSelectElement: Decoder[SlackBlockStaticSelectElement] =
      deriveDecoder[SlackBlockStaticSelectElement]

    implicit val encoderSlackBlockRichTextSection: Encoder.AsObject[SlackBlockRichTextSection] =
      deriveEncoder[SlackBlockRichTextSection]

    implicit val decoderSlackBlockRichTextSection: Decoder[SlackBlockRichTextSection] =
      deriveDecoder[SlackBlockRichTextSection]

    implicit val encoderSlackBlockRichTextPreformatted: Encoder.AsObject[SlackBlockRichTextPreformatted] =
      deriveEncoder[SlackBlockRichTextPreformatted]

    implicit val decoderSlackBlockRichTextPreformatted: Decoder[SlackBlockRichTextPreformatted] =
      deriveDecoder[SlackBlockRichTextPreformatted]

    implicit val encoderSlackConversationType: Encoder[SlackConversationType] =
      JsonTaggedAdtCodec.createPureEnumEncoder[SlackConversationType]()

    implicit val decoderSlackConversationType: Decoder[SlackConversationType] =
      JsonTaggedAdtCodec.createPureEnumDecoder[SlackConversationType]()

    implicit val encoderSlackConversationListFilter: Encoder.AsObject[SlackConversationListFilter] =
      deriveEncoder[SlackConversationListFilter]

    implicit val decoderSlackConversationListFilter: Decoder[SlackConversationListFilter] =
      deriveDecoder[SlackConversationListFilter]

    implicit val encoderSlackBlockConversationListSelectElement
        : Encoder.AsObject[SlackBlockConversationListSelectElement] =
      deriveEncoder[SlackBlockConversationListSelectElement]

    implicit val decoderSlackBlockConversationListSelectElement: Decoder[SlackBlockConversationListSelectElement] =
      deriveDecoder[SlackBlockConversationListSelectElement]

    implicit val encoderSlackBlockChannelsListSelectElement: Encoder.AsObject[SlackBlockChannelsListSelectElement] =
      deriveEncoder[SlackBlockChannelsListSelectElement]

    implicit val decoderSlackBlockChannelsListSelectElement: Decoder[SlackBlockChannelsListSelectElement] =
      deriveDecoder[SlackBlockChannelsListSelectElement]

    implicit val encoderSlackBlockRichTextQuote: Encoder.AsObject[SlackBlockRichTextQuote] =
      deriveEncoder[SlackBlockRichTextQuote]

    implicit val decoderSlackBlockRichTextQuote: Decoder[SlackBlockRichTextQuote] =
      deriveDecoder[SlackBlockRichTextQuote]

    implicit val encoderSlackBlockExternalSelectElement: Encoder.AsObject[SlackBlockExternalSelectElement] =
      deriveEncoder[SlackBlockExternalSelectElement]

    implicit val decoderSlackBlockExternalSelectElement: Decoder[SlackBlockExternalSelectElement] =
      deriveDecoder[SlackBlockExternalSelectElement]

    implicit val encoderSlackBlockOverflowSelectElement: Encoder.AsObject[SlackBlockOverflowElement] =
      deriveEncoder[SlackBlockOverflowElement]

    implicit val decoderSlackBlockOverflowSelectElement: Decoder[SlackBlockOverflowElement] =
      deriveDecoder[SlackBlockOverflowElement]

    implicit val encoderSlackBlockPlainInputElement: Encoder.AsObject[SlackBlockPlainInputElement] =
      deriveEncoder[SlackBlockPlainInputElement]

    implicit val decoderSlackBlockPlainInputElement: Decoder[SlackBlockPlainInputElement] =
      deriveDecoder[SlackBlockPlainInputElement]

    implicit val encoderSlackBlockDatePickerElement: Encoder.AsObject[SlackBlockDatePickerElement] =
      deriveEncoder[SlackBlockDatePickerElement]

    implicit val decoderSlackBlockDatePickerElement: Decoder[SlackBlockDatePickerElement] =
      deriveDecoder[SlackBlockDatePickerElement]

    implicit val encoderSlackBlockUsersListSelectElement: Encoder.AsObject[SlackBlockUsersListSelectElement] =
      deriveEncoder[SlackBlockUsersListSelectElement]

    implicit val decoderSlackBlockUsersListSelectElement: Decoder[SlackBlockUsersListSelectElement] =
      deriveDecoder[SlackBlockUsersListSelectElement]

    implicit val encoderSlackBlockMultiStaticSelectElement: Encoder.AsObject[SlackBlockMultiStaticSelectElement] =
      deriveEncoder[SlackBlockMultiStaticSelectElement]

    implicit val decoderSlackBlockMultiStaticSelectElement: Decoder[SlackBlockMultiStaticSelectElement] =
      deriveDecoder[SlackBlockMultiStaticSelectElement]

    implicit val encoderSlackBlockMultiExternalSelectElement: Encoder.AsObject[SlackBlockMultiExternalSelectElement] =
      deriveEncoder[SlackBlockMultiExternalSelectElement]

    implicit val decoderSlackBlockMultiExternalSelectElement: Decoder[SlackBlockMultiExternalSelectElement] =
      deriveDecoder[SlackBlockMultiExternalSelectElement]

    implicit val encoderSlackBlockMultiUsersListSelectElement: Encoder.AsObject[SlackBlockMultiUsersListSelectElement] =
      deriveEncoder[SlackBlockMultiUsersListSelectElement]

    implicit val decoderSlackBlockMultiUsersListSelectElement: Decoder[SlackBlockMultiUsersListSelectElement] =
      deriveDecoder[SlackBlockMultiUsersListSelectElement]

    implicit val encoderSlackBlockMultiConversationListSelectElement
        : Encoder.AsObject[SlackBlockMultiConversationListSelectElement] =
      deriveEncoder[SlackBlockMultiConversationListSelectElement]

    implicit val decoderSlackBlockMultiConversationListSelectElement
        : Decoder[SlackBlockMultiConversationListSelectElement] =
      deriveDecoder[SlackBlockMultiConversationListSelectElement]

    implicit val encoderSlackBlockMultiChannelsListSelectElement
        : Encoder.AsObject[SlackBlockMultiChannelsListSelectElement] =
      deriveEncoder[SlackBlockMultiChannelsListSelectElement]

    implicit val decoderSlackBlockMultiChannelsListSelectElement: Decoder[SlackBlockMultiChannelsListSelectElement] =
      deriveDecoder[SlackBlockMultiChannelsListSelectElement]

    implicit val encoderSlackBlockRadioButtonsElement: Encoder.AsObject[SlackBlockRadioButtonsElement] =
      deriveEncoder[SlackBlockRadioButtonsElement]

    implicit val decoderSlackBlockRadioButtonsElement: Decoder[SlackBlockRadioButtonsElement] =
      deriveDecoder[SlackBlockRadioButtonsElement]

    implicit val encoderSlackBlockCheckboxesElement: Encoder.AsObject[SlackBlockCheckboxesElement] =
      deriveEncoder[SlackBlockCheckboxesElement]

    implicit val decoderSlackBlockCheckboxesElement: Decoder[SlackBlockCheckboxesElement] =
      deriveDecoder[SlackBlockCheckboxesElement]

    implicit val encoderSlackBlockElement: Encoder[SlackBlockElement] =
      JsonTaggedAdtCodec.createEncoder[SlackBlockElement]( "type" )

    implicit val decoderSlackBlockElement: Decoder[SlackBlockElement] =
      JsonTaggedAdtCodec.createDecoder[SlackBlockElement]( "type" )

    implicit val encoderSlackMessageReaction: Encoder.AsObject[SlackMessageReaction] =
      deriveEncoder[SlackMessageReaction]
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

    implicit val encoderSlackChannelJoinMessage: Encoder.AsObject[SlackChannelJoinMessage] =
      deriveEncoder[SlackChannelJoinMessage]

    implicit val decoderSlackChannelJoinMessage: Decoder[SlackChannelJoinMessage] =
      deriveDecoder[SlackChannelJoinMessage]

    implicit val encoderSlackBotAddMessage: Encoder.AsObject[SlackBotAddMessage] =
      deriveEncoder[SlackBotAddMessage]

    implicit val decoderSlackBotAddMessage: Decoder[SlackBotAddMessage] =
      deriveDecoder[SlackBotAddMessage]

    implicit val encoderSlackChannelTopicMessage: Encoder.AsObject[SlackChannelTopicMessage] =
      deriveEncoder[SlackChannelTopicMessage]

    implicit val decoderSlackChannelTopicMessage: Decoder[SlackChannelTopicMessage] =
      deriveDecoder[SlackChannelTopicMessage]

    implicit val encoderSlackChannelPurposeMessage: Encoder.AsObject[SlackChannelPurposeMessage] =
      deriveEncoder[SlackChannelPurposeMessage]

    implicit val decoderSlackChannelPurposeMessage: Decoder[SlackChannelPurposeMessage] =
      deriveDecoder[SlackChannelPurposeMessage]

    implicit val encoderSlackChannelNameMessage: Encoder.AsObject[SlackChannelNameMessage] =
      deriveEncoder[SlackChannelNameMessage]

    implicit val decoderSlackChannelNameMessage: Decoder[SlackChannelNameMessage] =
      deriveDecoder[SlackChannelNameMessage]

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
        messageDecoderDefinition[SlackMessage] { cursor: HCursor => cursor.as[SlackUserMessage] }
      }

    val SLACK_TYPE_VALUE = "message"

    implicit val encoderPinnedMessage: Encoder.AsObject[SlackPinnedMessage] =
      JsonTaggedAdtCodec.createEncoderDefinition[SlackPinnedMessage](
        messageEncoderDefinition[SlackPinnedMessage]
      )

    implicit val decoderPinnedMessage: Decoder[SlackPinnedMessage] =
      JsonTaggedAdtCodec.createDecoderDefinition[SlackPinnedMessage] {
        messageDecoderDefinition[SlackPinnedMessage] { cursor: HCursor => cursor.as[SlackUserMessage] }
      }

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
        messageEncoderDefinition[SlackMessageEvent]
      )

    implicit val decoderMessageEvent: Decoder[SlackMessageEvent] =
      JsonTaggedAdtCodec.createDecoderDefinition[SlackMessageEvent] {
        messageDecoderDefinition[SlackMessageEvent] { cursor: HCursor => cursor.as[SlackUserMessage] }
      }

    implicit val encoderSlackModalView: Encoder.AsObject[SlackModalView] = deriveEncoder[SlackModalView]
    implicit val decoderSlackModalView: Decoder[SlackModalView] = deriveDecoder[SlackModalView]
    implicit val encoderSlackHomeView: Encoder.AsObject[SlackHomeView] = deriveEncoder[SlackHomeView]
    implicit val decoderSlackHomeView: Decoder[SlackHomeView] = deriveDecoder[SlackHomeView]

    implicit val encoderSlackView: Encoder.AsObject[SlackView] = JsonTaggedAdtCodec.createEncoder[SlackView]( "type" )
    implicit val decoderSlackView: Decoder[SlackView] = JsonTaggedAdtCodec.createDecoder[SlackView]( "type" )

    implicit val encoderSlackViewState: Encoder.AsObject[SlackViewState] = deriveEncoder[SlackViewState]
    implicit val decoderSlackViewState: Decoder[SlackViewState] = deriveDecoder[SlackViewState]

    implicit val encoderSlackStatefulViewParams: Encoder.AsObject[SlackStatefulStateParams] =
      deriveEncoder[SlackStatefulStateParams]

    def createSlackStatefulViewEncoder(): Encoder.AsObject[SlackStatefulView] = (model: SlackStatefulView) => {
      encoderSlackView
        .encodeObject( model.view )
        .deepMerge( encoderSlackStatefulViewParams.encodeObject( model.stateParams ) )
    }

    def createSlackStatefulViewDecoder(): Decoder[SlackStatefulView] = (cursor: HCursor) => {
      for {
        view <- cursor.as[SlackView]
        stateParams <- cursor.as[SlackStatefulStateParams]( deriveDecoder[SlackStatefulStateParams] )
      } yield SlackStatefulView(
        stateParams,
        view
      )
    }

    implicit val encoderSlackStatefulView = createSlackStatefulViewEncoder()
    implicit val decoderSlackStatefulView = createSlackStatefulViewDecoder()

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

    implicit val encoderSlackEventCallbackBody: Encoder.AsObject[SlackEventCallbackBody] =
      JsonTaggedAdtCodec.createEncoder[SlackEventCallbackBody]( "type" )

    implicit val decoderSlackEventCallbackBody: Decoder[SlackEventCallbackBody] =
      JsonTaggedAdtCodec.createDecoder[SlackEventCallbackBody]( "type" )

    implicit val encoderSlackInteractionActionInfo: Encoder.AsObject[SlackInteractionActionInfo] =
      deriveEncoder[SlackInteractionActionInfo]

    implicit val decoderSlackInteractionActionInfo: Decoder[SlackInteractionActionInfo] =
      deriveDecoder[SlackInteractionActionInfo]

    implicit val encoderSlackInteractionViewClosedEvent: Encoder.AsObject[SlackInteractionViewClosedEvent] =
      deriveEncoder[SlackInteractionViewClosedEvent]

    implicit val decoderSlackInteractionViewClosedEvent: Decoder[SlackInteractionViewClosedEvent] =
      deriveDecoder[SlackInteractionViewClosedEvent]

    implicit val encoderSlackInteractionViewSubmissionEvent: Encoder.AsObject[SlackInteractionViewSubmissionEvent] =
      deriveEncoder[SlackInteractionViewSubmissionEvent]

    implicit val decoderSlackInteractionViewSubmissionEvent: Decoder[SlackInteractionViewSubmissionEvent] =
      deriveDecoder[SlackInteractionViewSubmissionEvent]

    implicit val encoderSlackInteractionBlockActionEvent: Encoder.AsObject[SlackInteractionBlockActionEvent] =
      deriveEncoder[SlackInteractionBlockActionEvent]

    implicit val decoderSlackInteractionBlockActionEvent: Decoder[SlackInteractionBlockActionEvent] =
      deriveDecoder[SlackInteractionBlockActionEvent]

    implicit val encoderSlackInteractionMessageActionEvent: Encoder.AsObject[SlackInteractionMessageActionEvent] =
      deriveEncoder[SlackInteractionMessageActionEvent]

    implicit val decoderSlackInteractionMessageActionEvent: Decoder[SlackInteractionMessageActionEvent] =
      deriveDecoder[SlackInteractionMessageActionEvent]

    implicit val encoderSlackInteractionShortcutEvent: Encoder.AsObject[SlackInteractionShortcutEvent] =
      deriveEncoder[SlackInteractionShortcutEvent]

    implicit val decoderSlackInteractionShortcutEvent: Decoder[SlackInteractionShortcutEvent] =
      deriveDecoder[SlackInteractionShortcutEvent]

    implicit val encoderSlackInteractionDialogueSubmissionEvent
        : Encoder.AsObject[SlackInteractionDialogueSubmissionEvent] =
      deriveEncoder[SlackInteractionDialogueSubmissionEvent]

    implicit val decoderSlackInteractionDialogueSubmissionEvent: Decoder[SlackInteractionDialogueSubmissionEvent] =
      deriveDecoder[SlackInteractionDialogueSubmissionEvent]

    implicit val encoderSlackInteractionEvent: Encoder[SlackInteractionEvent] =
      JsonTaggedAdtCodec.createEncoder[SlackInteractionEvent]( "type" )

    implicit val decoderSlackInteractionEvent: Decoder[SlackInteractionEvent] =
      JsonTaggedAdtCodec.createDecoder[SlackInteractionEvent]( "type" )

    implicit val encoderSlackInteractionActionMessageContainer
        : Encoder.AsObject[SlackInteractionActionMessageContainer] =
      deriveEncoder[SlackInteractionActionMessageContainer]

    implicit val decoderSlackInteractionActionMessageContainer: Decoder[SlackInteractionActionMessageContainer] =
      deriveDecoder[SlackInteractionActionMessageContainer]

    implicit val encoderSlackInteractionActionViewContainer: Encoder.AsObject[SlackInteractionActionViewContainer] =
      deriveEncoder[SlackInteractionActionViewContainer]

    implicit val decoderSlackInteractionActionViewContainer: Decoder[SlackInteractionActionViewContainer] =
      deriveDecoder[SlackInteractionActionViewContainer]

    implicit val encoderSlackInteractionActionContainer: Encoder[SlackInteractionActionContainer] =
      JsonTaggedAdtCodec.createEncoder[SlackInteractionActionContainer]( "type" )

    implicit val decoderSlackInteractionActionContainer: Decoder[SlackInteractionActionContainer] =
      JsonTaggedAdtCodec.createDecoder[SlackInteractionActionContainer]( "type" )

    implicit val encoderSlackUrlVerificationEvent: Encoder.AsObject[SlackUrlVerificationEvent] =
      deriveEncoder[SlackUrlVerificationEvent]

    implicit val decoderSlackUrlVerificationEvent: Decoder[SlackUrlVerificationEvent] =
      deriveDecoder[SlackUrlVerificationEvent]

    implicit val encoderSlackEventCallback: Encoder.AsObject[SlackEventCallback] = deriveEncoder[SlackEventCallback]
    implicit val decoderSlackEventCallback: Decoder[SlackEventCallback] = deriveDecoder[SlackEventCallback]

    implicit val encoderSlackAppRateLimitedEvent: Encoder.AsObject[SlackAppRateLimitedEvent] =
      deriveEncoder[SlackAppRateLimitedEvent]

    implicit val decoderSlackAppRateLimitedEvent: Decoder[SlackAppRateLimitedEvent] =
      deriveDecoder[SlackAppRateLimitedEvent]

    implicit val encoderPushEvent = JsonTaggedAdtCodec.createEncoder[SlackPushEvent]( "type" )
    implicit val decoderPushEvent = JsonTaggedAdtCodec.createDecoder[SlackPushEvent]( "type" )

    implicit val encoderSlackApiUninstall: Encoder.AsObject[SlackApiUninstallRequest] =
      deriveEncoder[SlackApiUninstallRequest]
    implicit val decoderSlackApiUninstall: Decoder[SlackApiUninstallRequest] = deriveDecoder[SlackApiUninstallRequest]

    implicit val encoderSlackApiUninstallResponse: Encoder.AsObject[SlackApiUninstallResponse] =
      deriveEncoder[SlackApiUninstallResponse]

    implicit val decoderSlackApiUninstallResponse: Decoder[SlackApiUninstallResponse] =
      deriveDecoder[SlackApiUninstallResponse]

    implicit val encoderSlackApiAuthRevokeRequest: Encoder.AsObject[SlackApiAuthRevokeRequest] =
      deriveEncoder[SlackApiAuthRevokeRequest]

    implicit val decoderSlackApiAuthRevokeRequest: Decoder[SlackApiAuthRevokeRequest] =
      deriveDecoder[SlackApiAuthRevokeRequest]

    implicit val encoderSlackApiAuthRevokeResponse: Encoder.AsObject[SlackApiAuthRevokeResponse] =
      deriveEncoder[SlackApiAuthRevokeResponse]

    implicit val decoderSlackApiAuthRevokeResponse: Decoder[SlackApiAuthRevokeResponse] =
      deriveDecoder[SlackApiAuthRevokeResponse]

    implicit val encoderSlackApiAuthTestResponse: Encoder.AsObject[SlackApiAuthTestResponse] =
      deriveEncoder[SlackApiAuthTestResponse]

    implicit val decoderSlackApiAuthTestResponse: Decoder[SlackApiAuthTestResponse] =
      deriveDecoder[SlackApiAuthTestResponse]

    implicit val encoderSlackApiBotsInfo: Encoder.AsObject[SlackApiBotsInfo] = deriveEncoder[SlackApiBotsInfo]
    implicit val decoderSlackApiBotsInfo: Decoder[SlackApiBotsInfo] = deriveDecoder[SlackApiBotsInfo]

    implicit val encoderSlackApiBotsProfile: Encoder.AsObject[SlackApiBotsProfile] = deriveEncoder[SlackApiBotsProfile]
    implicit val decoderSlackApiBotsProfile: Decoder[SlackApiBotsProfile] = deriveDecoder[SlackApiBotsProfile]

    implicit val encoderSlackApiChannelsArchiveRequest: Encoder.AsObject[SlackApiChannelsArchiveRequest] =
      deriveEncoder[SlackApiChannelsArchiveRequest]

    implicit val decoderSlackApiChannelsArchiveRequest: Decoder[SlackApiChannelsArchiveRequest] =
      deriveDecoder[SlackApiChannelsArchiveRequest]

    implicit val encoderSlackApiChannelsArchiveResponse: Encoder.AsObject[SlackApiChannelsArchiveResponse] =
      deriveEncoder[SlackApiChannelsArchiveResponse]

    implicit val decoderSlackApiChannelsArchiveResponse: Decoder[SlackApiChannelsArchiveResponse] =
      deriveDecoder[SlackApiChannelsArchiveResponse]

    implicit val encoderSlackApiChannelsCreateRequest: Encoder.AsObject[SlackApiChannelsCreateRequest] =
      deriveEncoder[SlackApiChannelsCreateRequest]

    implicit val decoderSlackApiChannelsCreateRequest: Decoder[SlackApiChannelsCreateRequest] =
      deriveDecoder[SlackApiChannelsCreateRequest]

    implicit val encoderSlackApiChannelsCreateResponse: Encoder.AsObject[SlackApiChannelsCreateResponse] =
      deriveEncoder[SlackApiChannelsCreateResponse]

    implicit val decoderSlackApiChannelsCreateResponse: Decoder[SlackApiChannelsCreateResponse] =
      deriveDecoder[SlackApiChannelsCreateResponse]

    implicit val encoderSlackApiChannelsHistoryRequest: Encoder.AsObject[SlackApiChannelsHistoryRequest] =
      deriveEncoder[SlackApiChannelsHistoryRequest]

    implicit val decoderSlackApiChannelsHistoryRequest: Decoder[SlackApiChannelsHistoryRequest] =
      deriveDecoder[SlackApiChannelsHistoryRequest]

    implicit val encoderSlackApiChannelsHistoryResponse: Encoder.AsObject[SlackApiChannelsHistoryResponse] =
      deriveEncoder[SlackApiChannelsHistoryResponse]

    implicit val decoderSlackApiChannelsHistoryResponse: Decoder[SlackApiChannelsHistoryResponse] =
      deriveDecoder[SlackApiChannelsHistoryResponse]

    implicit val encoderSlackApiChannelsInfoRequest: Encoder.AsObject[SlackApiChannelsInfoRequest] =
      deriveEncoder[SlackApiChannelsInfoRequest]

    implicit val decoderSlackApiChannelsInfoRequest: Decoder[SlackApiChannelsInfoRequest] =
      deriveDecoder[SlackApiChannelsInfoRequest]

    implicit val encoderSlackApiChannelsInfoResponse: Encoder.AsObject[SlackApiChannelsInfoResponse] =
      deriveEncoder[SlackApiChannelsInfoResponse]

    implicit val decoderSlackApiChannelsInfoResponse: Decoder[SlackApiChannelsInfoResponse] =
      deriveDecoder[SlackApiChannelsInfoResponse]

    implicit val encoderSlackApiChannelsInviteRequest: Encoder.AsObject[SlackApiChannelsInviteRequest] =
      deriveEncoder[SlackApiChannelsInviteRequest]

    implicit val decoderSlackApiChannelsInviteRequest: Decoder[SlackApiChannelsInviteRequest] =
      deriveDecoder[SlackApiChannelsInviteRequest]

    implicit val encoderSlackApiChannelsInviteResponse: Encoder.AsObject[SlackApiChannelsInviteResponse] =
      deriveEncoder[SlackApiChannelsInviteResponse]

    implicit val decoderSlackApiChannelsInviteResponse: Decoder[SlackApiChannelsInviteResponse] =
      deriveDecoder[SlackApiChannelsInviteResponse]

    implicit val encoderSlackApiChannelsJoinRequest: Encoder.AsObject[SlackApiChannelsJoinRequest] =
      deriveEncoder[SlackApiChannelsJoinRequest]

    implicit val decoderSlackApiChannelsJoinRequest: Decoder[SlackApiChannelsJoinRequest] =
      deriveDecoder[SlackApiChannelsJoinRequest]

    implicit val encoderSlackApiChannelsJoinResponse: Encoder.AsObject[SlackApiChannelsJoinResponse] =
      deriveEncoder[SlackApiChannelsJoinResponse]

    implicit val decoderSlackApiChannelsJoinResponse: Decoder[SlackApiChannelsJoinResponse] =
      deriveDecoder[SlackApiChannelsJoinResponse]

    implicit val encoderSlackApiChannelsKickRequest: Encoder.AsObject[SlackApiChannelsKickRequest] =
      deriveEncoder[SlackApiChannelsKickRequest]

    implicit val decoderSlackApiChannelsKickRequest: Decoder[SlackApiChannelsKickRequest] =
      deriveDecoder[SlackApiChannelsKickRequest]

    implicit val encoderSlackApiChannelsKickResponse: Encoder.AsObject[SlackApiChannelsKickResponse] =
      deriveEncoder[SlackApiChannelsKickResponse]

    implicit val decoderSlackApiChannelsKickResponse: Decoder[SlackApiChannelsKickResponse] =
      deriveDecoder[SlackApiChannelsKickResponse]

    implicit val encoderSlackApiChannelsLeaveRequest: Encoder.AsObject[SlackApiChannelsLeaveRequest] =
      deriveEncoder[SlackApiChannelsLeaveRequest]

    implicit val decoderSlackApiChannelsLeaveRequest: Decoder[SlackApiChannelsLeaveRequest] =
      deriveDecoder[SlackApiChannelsLeaveRequest]

    implicit val encoderSlackApiChannelsLeaveResponse: Encoder.AsObject[SlackApiChannelsLeaveResponse] =
      deriveEncoder[SlackApiChannelsLeaveResponse]

    implicit val decoderSlackApiChannelsLeaveResponse: Decoder[SlackApiChannelsLeaveResponse] =
      deriveDecoder[SlackApiChannelsLeaveResponse]

    implicit val encoderSlackApiChannelsListRequest: Encoder.AsObject[SlackApiChannelsListRequest] =
      deriveEncoder[SlackApiChannelsListRequest]

    implicit val decoderSlackApiChannelsListRequest: Decoder[SlackApiChannelsListRequest] =
      deriveDecoder[SlackApiChannelsListRequest]

    implicit val encoderSlackApiChannelsListResponse: Encoder.AsObject[SlackApiChannelsListResponse] =
      deriveEncoder[SlackApiChannelsListResponse]

    implicit val decoderSlackApiChannelsListResponse: Decoder[SlackApiChannelsListResponse] =
      deriveDecoder[SlackApiChannelsListResponse]

    implicit val encoderSlackApiChannelsMarkRequest: Encoder.AsObject[SlackApiChannelsMarkRequest] =
      deriveEncoder[SlackApiChannelsMarkRequest]

    implicit val decoderSlackApiChannelsMarkRequest: Decoder[SlackApiChannelsMarkRequest] =
      deriveDecoder[SlackApiChannelsMarkRequest]

    implicit val encoderSlackApiChannelsMarkResponse: Encoder.AsObject[SlackApiChannelsMarkResponse] =
      deriveEncoder[SlackApiChannelsMarkResponse]

    implicit val decoderSlackApiChannelsMarkResponse: Decoder[SlackApiChannelsMarkResponse] =
      deriveDecoder[SlackApiChannelsMarkResponse]

    implicit val encoderSlackApiChannelsRenameRequest: Encoder.AsObject[SlackApiChannelsRenameRequest] =
      deriveEncoder[SlackApiChannelsRenameRequest]

    implicit val decoderSlackApiChannelsRenameRequest: Decoder[SlackApiChannelsRenameRequest] =
      deriveDecoder[SlackApiChannelsRenameRequest]

    implicit val encoderSlackApiChannelsRenameResponse: Encoder.AsObject[SlackApiChannelsRenameResponse] =
      deriveEncoder[SlackApiChannelsRenameResponse]

    implicit val decoderSlackApiChannelsRenameResponse: Decoder[SlackApiChannelsRenameResponse] =
      deriveDecoder[SlackApiChannelsRenameResponse]

    implicit val encoderSlackApiChannelsRepliesRequest: Encoder.AsObject[SlackApiChannelsRepliesRequest] =
      deriveEncoder[SlackApiChannelsRepliesRequest]

    implicit val decoderSlackApiChannelsRepliesRequest: Decoder[SlackApiChannelsRepliesRequest] =
      deriveDecoder[SlackApiChannelsRepliesRequest]

    implicit val encoderSlackApiChannelsRepliesResponse: Encoder.AsObject[SlackApiChannelsRepliesResponse] =
      deriveEncoder[SlackApiChannelsRepliesResponse]

    implicit val decoderSlackApiChannelsRepliesResponse: Decoder[SlackApiChannelsRepliesResponse] =
      deriveDecoder[SlackApiChannelsRepliesResponse]

    implicit val encoderSlackApiChannelsSetPurposeRequest: Encoder.AsObject[SlackApiChannelsSetPurposeRequest] =
      deriveEncoder[SlackApiChannelsSetPurposeRequest]

    implicit val decoderSlackApiChannelsSetPurposeRequest: Decoder[SlackApiChannelsSetPurposeRequest] =
      deriveDecoder[SlackApiChannelsSetPurposeRequest]

    implicit val encoderSlackApiChannelsSetPurposeResponse: Encoder.AsObject[SlackApiChannelsSetPurposeResponse] =
      deriveEncoder[SlackApiChannelsSetPurposeResponse]

    implicit val decoderSlackApiChannelsSetPurposeResponse: Decoder[SlackApiChannelsSetPurposeResponse] =
      deriveDecoder[SlackApiChannelsSetPurposeResponse]

    implicit val encoderSlackApiChannelsSetTopicRequest: Encoder.AsObject[SlackApiChannelsSetTopicRequest] =
      deriveEncoder[SlackApiChannelsSetTopicRequest]

    implicit val decoderSlackApiChannelsSetTopicRequest: Decoder[SlackApiChannelsSetTopicRequest] =
      deriveDecoder[SlackApiChannelsSetTopicRequest]

    implicit val encoderSlackApiChannelsSetTopicResponse: Encoder.AsObject[SlackApiChannelsSetTopicResponse] =
      deriveEncoder[SlackApiChannelsSetTopicResponse]

    implicit val decoderSlackApiChannelsSetTopicResponse: Decoder[SlackApiChannelsSetTopicResponse] =
      deriveDecoder[SlackApiChannelsSetTopicResponse]

    implicit val encoderSlackApiChannelsUnarchiveRequest: Encoder.AsObject[SlackApiChannelsUnarchiveRequest] =
      deriveEncoder[SlackApiChannelsUnarchiveRequest]

    implicit val decoderSlackApiChannelsUnarchiveRequest: Decoder[SlackApiChannelsUnarchiveRequest] =
      deriveDecoder[SlackApiChannelsUnarchiveRequest]

    implicit val encoderSlackApiChannelsUnarchiveResponse: Encoder.AsObject[SlackApiChannelsUnarchiveResponse] =
      deriveEncoder[SlackApiChannelsUnarchiveResponse]

    implicit val decoderSlackApiChannelsUnarchiveResponse: Decoder[SlackApiChannelsUnarchiveResponse] =
      deriveDecoder[SlackApiChannelsUnarchiveResponse]

    implicit val encoderSlackApiChatDeleteRequest: Encoder.AsObject[SlackApiChatDeleteRequest] =
      deriveEncoder[SlackApiChatDeleteRequest]

    implicit val decoderSlackApiChatDeleteRequest: Decoder[SlackApiChatDeleteRequest] =
      deriveDecoder[SlackApiChatDeleteRequest]

    implicit val encoderSlackApiChatDeleteResponse: Encoder.AsObject[SlackApiChatDeleteResponse] =
      deriveEncoder[SlackApiChatDeleteResponse]

    implicit val decoderSlackApiChatDeleteResponse: Decoder[SlackApiChatDeleteResponse] =
      deriveDecoder[SlackApiChatDeleteResponse]

    implicit val encoderSlackApiChatDeleteScheduledMessageRequest
        : Encoder.AsObject[SlackApiChatDeleteScheduledMessageRequest] =
      deriveEncoder[SlackApiChatDeleteScheduledMessageRequest]

    implicit val decoderSlackApiChatDeleteScheduledMessageRequest: Decoder[SlackApiChatDeleteScheduledMessageRequest] =
      deriveDecoder[SlackApiChatDeleteScheduledMessageRequest]

    implicit val encoderSlackApiChatDeleteScheduledMessageResponse
        : Encoder.AsObject[SlackApiChatDeleteScheduledMessageResponse] =
      deriveEncoder[SlackApiChatDeleteScheduledMessageResponse]

    implicit val decoderSlackApiChatDeleteScheduledMessageResponse
        : Decoder[SlackApiChatDeleteScheduledMessageResponse] =
      deriveDecoder[SlackApiChatDeleteScheduledMessageResponse]

    implicit val encoderSlackApiChatGetPermalinkRequest: Encoder.AsObject[SlackApiChatGetPermalinkRequest] =
      deriveEncoder[SlackApiChatGetPermalinkRequest]

    implicit val decoderSlackApiChatGetPermalinkRequest: Decoder[SlackApiChatGetPermalinkRequest] =
      deriveDecoder[SlackApiChatGetPermalinkRequest]

    implicit val encoderSlackApiChatGetPermalinkResponse: Encoder.AsObject[SlackApiChatGetPermalinkResponse] =
      deriveEncoder[SlackApiChatGetPermalinkResponse]

    implicit val decoderSlackApiChatGetPermalinkResponse: Decoder[SlackApiChatGetPermalinkResponse] =
      deriveDecoder[SlackApiChatGetPermalinkResponse]

    implicit val encoderSlackApiChatMeMessageRequest: Encoder.AsObject[SlackApiChatMeMessageRequest] =
      deriveEncoder[SlackApiChatMeMessageRequest]

    implicit val decoderSlackApiChatMeMessageRequest: Decoder[SlackApiChatMeMessageRequest] =
      deriveDecoder[SlackApiChatMeMessageRequest]

    implicit val encoderSlackApiChatMeMessageResponse: Encoder.AsObject[SlackApiChatMeMessageResponse] =
      deriveEncoder[SlackApiChatMeMessageResponse]

    implicit val decoderSlackApiChatMeMessageResponse: Decoder[SlackApiChatMeMessageResponse] =
      deriveDecoder[SlackApiChatMeMessageResponse]

    implicit val encoderSlackApiChatPostEphemeralRequest: Encoder.AsObject[SlackApiChatPostEphemeralRequest] =
      deriveEncoder[SlackApiChatPostEphemeralRequest]

    implicit val decoderSlackApiChatPostEphemeralRequest: Decoder[SlackApiChatPostEphemeralRequest] =
      deriveDecoder[SlackApiChatPostEphemeralRequest]

    implicit val encoderSlackApiChatPostEphemeralResponse: Encoder.AsObject[SlackApiChatPostEphemeralResponse] =
      deriveEncoder[SlackApiChatPostEphemeralResponse]

    implicit val decoderSlackApiChatPostEphemeralResponse: Decoder[SlackApiChatPostEphemeralResponse] =
      deriveDecoder[SlackApiChatPostEphemeralResponse]

    implicit val encoderSlackApiChatPostMessageRequest: Encoder.AsObject[SlackApiChatPostMessageRequest] =
      deriveEncoder[SlackApiChatPostMessageRequest]

    implicit val decoderSlackApiChatPostMessageRequest: Decoder[SlackApiChatPostMessageRequest] =
      deriveDecoder[SlackApiChatPostMessageRequest]

    implicit val encoderSlackApiChatPostMessageResponse: Encoder.AsObject[SlackApiChatPostMessageResponse] =
      deriveEncoder[SlackApiChatPostMessageResponse]

    implicit val decoderSlackApiChatPostMessageResponse: Decoder[SlackApiChatPostMessageResponse] =
      deriveDecoder[SlackApiChatPostMessageResponse]

    implicit val encoderSlackApiChatScheduledMessagesListRequest
        : Encoder.AsObject[SlackApiChatScheduledMessagesListRequest] =
      deriveEncoder[SlackApiChatScheduledMessagesListRequest]

    implicit val decoderSlackApiChatScheduledMessagesListRequest: Decoder[SlackApiChatScheduledMessagesListRequest] =
      deriveDecoder[SlackApiChatScheduledMessagesListRequest]

    implicit val encoderSlackApiChatScheduledMessageInfo: Encoder.AsObject[SlackApiChatScheduledMessageInfo] =
      deriveEncoder[SlackApiChatScheduledMessageInfo]

    implicit val decoderSlackApiChatScheduledMessageInfo: Decoder[SlackApiChatScheduledMessageInfo] =
      deriveDecoder[SlackApiChatScheduledMessageInfo]

    implicit val encoderSlackApiChatScheduledMessagesListResponse
        : Encoder.AsObject[SlackApiChatScheduledMessagesListResponse] =
      deriveEncoder[SlackApiChatScheduledMessagesListResponse]

    implicit val decoderSlackApiChatScheduledMessagesListResponse: Decoder[SlackApiChatScheduledMessagesListResponse] =
      deriveDecoder[SlackApiChatScheduledMessagesListResponse]

    implicit val encoderSlackApiChatScheduleMessageRequest: Encoder.AsObject[SlackApiChatScheduleMessageRequest] =
      deriveEncoder[SlackApiChatScheduleMessageRequest]

    implicit val decoderSlackApiChatScheduleMessageRequest: Decoder[SlackApiChatScheduleMessageRequest] =
      deriveDecoder[SlackApiChatScheduleMessageRequest]

    implicit val encoderSlackApiChatScheduleMessageResponse: Encoder.AsObject[SlackApiChatScheduleMessageResponse] =
      deriveEncoder[SlackApiChatScheduleMessageResponse]

    implicit val decoderSlackApiChatScheduleMessageResponse: Decoder[SlackApiChatScheduleMessageResponse] =
      deriveDecoder[SlackApiChatScheduleMessageResponse]

    implicit val encoderSlackApiChatUnfurlMapItem: Encoder.AsObject[SlackApiChatUnfurlMapItem] =
      deriveEncoder[SlackApiChatUnfurlMapItem]

    implicit val decoderSlackApiChatUnfurlMapItem: Decoder[SlackApiChatUnfurlMapItem] =
      deriveDecoder[SlackApiChatUnfurlMapItem]

    implicit val encoderSlackApiChatUnfurlRequest: Encoder.AsObject[SlackApiChatUnfurlRequest] =
      deriveEncoder[SlackApiChatUnfurlRequest]

    implicit val decoderSlackApiChatUnfurlRequest: Decoder[SlackApiChatUnfurlRequest] =
      deriveDecoder[SlackApiChatUnfurlRequest]

    implicit val encoderSlackApiChatUnfurlResponse: Encoder.AsObject[SlackApiChatUnfurlResponse] =
      deriveEncoder[SlackApiChatUnfurlResponse]

    implicit val decoderSlackApiChatUnfurlResponse: Decoder[SlackApiChatUnfurlResponse] =
      deriveDecoder[SlackApiChatUnfurlResponse]

    implicit val encoderSlackApiChatUpdateRequest: Encoder.AsObject[SlackApiChatUpdateRequest] =
      deriveEncoder[SlackApiChatUpdateRequest]

    implicit val decoderSlackApiChatUpdateRequest: Decoder[SlackApiChatUpdateRequest] =
      deriveDecoder[SlackApiChatUpdateRequest]

    implicit val encoderSlackApiChatUpdateResponse: Encoder.AsObject[SlackApiChatUpdateResponse] =
      deriveEncoder[SlackApiChatUpdateResponse]

    implicit val decoderSlackApiChatUpdateResponse: Decoder[SlackApiChatUpdateResponse] =
      deriveDecoder[SlackApiChatUpdateResponse]

    implicit val encoderSlackApiPostEventReply: Encoder.AsObject[SlackApiPostEventReply] =
      deriveEncoder[SlackApiPostEventReply]

    implicit val decoderSlackApiPostEventReply: Decoder[SlackApiPostEventReply] =
      deriveDecoder[SlackApiPostEventReply]

    implicit val encoderSlackApiPostEventReplyResponse: Encoder.AsObject[SlackApiPostEventReplyResponse] =
      deriveEncoder[SlackApiPostEventReplyResponse]

    implicit val decoderSlackApiPostEventReplyResponse: Decoder[SlackApiPostEventReplyResponse] =
      deriveDecoder[SlackApiPostEventReplyResponse]

    implicit val encoderSlackApiPostWebHookRequest: Encoder.AsObject[SlackApiPostWebHookRequest] =
      deriveEncoder[SlackApiPostWebHookRequest]

    implicit val decoderSlackApiPostWebHookRequest: Decoder[SlackApiPostWebHookRequest] =
      deriveDecoder[SlackApiPostWebHookRequest]

    implicit val encoderSlackApiPostWebHookResponse: Encoder.AsObject[SlackApiPostWebHookResponse] =
      deriveEncoder[SlackApiPostWebHookResponse]

    implicit val decoderSlackApiPostWebHookResponse: Decoder[SlackApiPostWebHookResponse] =
      deriveDecoder[SlackApiPostWebHookResponse]

    implicit val encoderSlackApiConversationsArchiveRequest: Encoder.AsObject[SlackApiConversationsArchiveRequest] =
      deriveEncoder[SlackApiConversationsArchiveRequest]

    implicit val decoderSlackApiConversationsArchiveRequest: Decoder[SlackApiConversationsArchiveRequest] =
      deriveDecoder[SlackApiConversationsArchiveRequest]

    implicit val encoderSlackApiConversationsArchiveResponse: Encoder.AsObject[SlackApiConversationsArchiveResponse] =
      deriveEncoder[SlackApiConversationsArchiveResponse]

    implicit val decoderSlackApiConversationsArchiveResponse: Decoder[SlackApiConversationsArchiveResponse] =
      deriveDecoder[SlackApiConversationsArchiveResponse]

    implicit val encoderSlackApiConversationsCloseRequest: Encoder.AsObject[SlackApiConversationsCloseRequest] =
      deriveEncoder[SlackApiConversationsCloseRequest]

    implicit val decoderSlackApiConversationsCloseRequest: Decoder[SlackApiConversationsCloseRequest] =
      deriveDecoder[SlackApiConversationsCloseRequest]

    implicit val encoderSlackApiConversationsCloseResponse: Encoder.AsObject[SlackApiConversationsCloseResponse] =
      deriveEncoder[SlackApiConversationsCloseResponse]

    implicit val decoderSlackApiConversationsCloseResponse: Decoder[SlackApiConversationsCloseResponse] =
      deriveDecoder[SlackApiConversationsCloseResponse]

    implicit val encoderSlackApiConversationsCreateRequest: Encoder.AsObject[SlackApiConversationsCreateRequest] =
      deriveEncoder[SlackApiConversationsCreateRequest]

    implicit val decoderSlackApiConversationsCreateRequest: Decoder[SlackApiConversationsCreateRequest] =
      deriveDecoder[SlackApiConversationsCreateRequest]

    implicit val encoderSlackApiConversationsCreateResponse: Encoder.AsObject[SlackApiConversationsCreateResponse] =
      deriveEncoder[SlackApiConversationsCreateResponse]

    implicit val decoderSlackApiConversationsCreateResponse: Decoder[SlackApiConversationsCreateResponse] =
      deriveDecoder[SlackApiConversationsCreateResponse]

    implicit val encoderSlackApiConversationsHistoryRequest: Encoder.AsObject[SlackApiConversationsHistoryRequest] =
      deriveEncoder[SlackApiConversationsHistoryRequest]

    implicit val decoderSlackApiConversationsHistoryRequest: Decoder[SlackApiConversationsHistoryRequest] =
      deriveDecoder[SlackApiConversationsHistoryRequest]

    implicit val encoderSlackApiConversationsHistoryResponse: Encoder.AsObject[SlackApiConversationsHistoryResponse] =
      deriveEncoder[SlackApiConversationsHistoryResponse]

    implicit val decoderSlackApiConversationsHistoryResponse: Decoder[SlackApiConversationsHistoryResponse] =
      deriveDecoder[SlackApiConversationsHistoryResponse]

    implicit val encoderSlackApiConversationsInfoRequest: Encoder.AsObject[SlackApiConversationsInfoRequest] =
      deriveEncoder[SlackApiConversationsInfoRequest]

    implicit val decoderSlackApiConversationsInfoRequest: Decoder[SlackApiConversationsInfoRequest] =
      deriveDecoder[SlackApiConversationsInfoRequest]

    implicit val encoderSlackApiConversationsInfoResponse: Encoder.AsObject[SlackApiConversationsInfoResponse] =
      deriveEncoder[SlackApiConversationsInfoResponse]

    implicit val decoderSlackApiConversationsInfoResponse: Decoder[SlackApiConversationsInfoResponse] =
      deriveDecoder[SlackApiConversationsInfoResponse]

    implicit val encoderSlackApiConversationsInviteRequest: Encoder.AsObject[SlackApiConversationsInviteRequest] =
      deriveEncoder[SlackApiConversationsInviteRequest]

    implicit val decoderSlackApiConversationsInviteRequest: Decoder[SlackApiConversationsInviteRequest] =
      deriveDecoder[SlackApiConversationsInviteRequest]

    implicit val encoderSlackApiConversationsInviteResponse: Encoder.AsObject[SlackApiConversationsInviteResponse] =
      deriveEncoder[SlackApiConversationsInviteResponse]

    implicit val decoderSlackApiConversationsInviteResponse: Decoder[SlackApiConversationsInviteResponse] =
      deriveDecoder[SlackApiConversationsInviteResponse]

    implicit val encoderSlackApiConversationsJoinRequest: Encoder.AsObject[SlackApiConversationsJoinRequest] =
      deriveEncoder[SlackApiConversationsJoinRequest]

    implicit val decoderSlackApiConversationsJoinRequest: Decoder[SlackApiConversationsJoinRequest] =
      deriveDecoder[SlackApiConversationsJoinRequest]

    implicit val encoderSlackApiConversationsJoinResponse: Encoder.AsObject[SlackApiConversationsJoinResponse] =
      deriveEncoder[SlackApiConversationsJoinResponse]

    implicit val decoderSlackApiConversationsJoinResponse: Decoder[SlackApiConversationsJoinResponse] =
      deriveDecoder[SlackApiConversationsJoinResponse]

    implicit val encoderSlackApiConversationsKickRequest: Encoder.AsObject[SlackApiConversationsKickRequest] =
      deriveEncoder[SlackApiConversationsKickRequest]

    implicit val decoderSlackApiConversationsKickRequest: Decoder[SlackApiConversationsKickRequest] =
      deriveDecoder[SlackApiConversationsKickRequest]

    implicit val encoderSlackApiConversationsKickResponse: Encoder.AsObject[SlackApiConversationsKickResponse] =
      deriveEncoder[SlackApiConversationsKickResponse]

    implicit val decoderSlackApiConversationsKickResponse: Decoder[SlackApiConversationsKickResponse] =
      deriveDecoder[SlackApiConversationsKickResponse]

    implicit val encoderSlackApiConversationsLeaveRequest: Encoder.AsObject[SlackApiConversationsLeaveRequest] =
      deriveEncoder[SlackApiConversationsLeaveRequest]

    implicit val decoderSlackApiConversationsLeaveRequest: Decoder[SlackApiConversationsLeaveRequest] =
      deriveDecoder[SlackApiConversationsLeaveRequest]

    implicit val encoderSlackApiConversationsLeaveResponse: Encoder.AsObject[SlackApiConversationsLeaveResponse] =
      deriveEncoder[SlackApiConversationsLeaveResponse]

    implicit val decoderSlackApiConversationsLeaveResponse: Decoder[SlackApiConversationsLeaveResponse] =
      deriveDecoder[SlackApiConversationsLeaveResponse]

    implicit val encoderSlackApiConversationsListRequest: Encoder.AsObject[SlackApiConversationsListRequest] =
      deriveEncoder[SlackApiConversationsListRequest]

    implicit val decoderSlackApiConversationsListRequest: Decoder[SlackApiConversationsListRequest] =
      deriveDecoder[SlackApiConversationsListRequest]

    implicit val encoderSlackApiConversationsListResponse: Encoder.AsObject[SlackApiConversationsListResponse] =
      deriveEncoder[SlackApiConversationsListResponse]

    implicit val decoderSlackApiConversationsListResponse: Decoder[SlackApiConversationsListResponse] =
      deriveDecoder[SlackApiConversationsListResponse]

    implicit val encoderSlackApiConversationsMembersRequest: Encoder.AsObject[SlackApiConversationsMembersRequest] =
      deriveEncoder[SlackApiConversationsMembersRequest]

    implicit val decoderSlackApiConversationsMembersRequest: Decoder[SlackApiConversationsMembersRequest] =
      deriveDecoder[SlackApiConversationsMembersRequest]

    implicit val encoderSlackApiConversationsMembersResponse: Encoder.AsObject[SlackApiConversationsMembersResponse] =
      deriveEncoder[SlackApiConversationsMembersResponse]

    implicit val decoderSlackApiConversationsMembersResponse: Decoder[SlackApiConversationsMembersResponse] =
      deriveDecoder[SlackApiConversationsMembersResponse]

    implicit val encoderSlackApiConversationsRenameRequest: Encoder.AsObject[SlackApiConversationsRenameRequest] =
      deriveEncoder[SlackApiConversationsRenameRequest]

    implicit val decoderSlackApiConversationsRenameRequest: Decoder[SlackApiConversationsRenameRequest] =
      deriveDecoder[SlackApiConversationsRenameRequest]

    implicit val encoderSlackApiConversationsRenameResponse: Encoder.AsObject[SlackApiConversationsRenameResponse] =
      deriveEncoder[SlackApiConversationsRenameResponse]

    implicit val decoderSlackApiConversationsRenameResponse: Decoder[SlackApiConversationsRenameResponse] =
      deriveDecoder[SlackApiConversationsRenameResponse]

    implicit val encoderSlackApiConversationsRepliesRequest: Encoder.AsObject[SlackApiConversationsRepliesRequest] =
      deriveEncoder[SlackApiConversationsRepliesRequest]

    implicit val decoderSlackApiConversationsRepliesRequest: Decoder[SlackApiConversationsRepliesRequest] =
      deriveDecoder[SlackApiConversationsRepliesRequest]

    implicit val encoderSlackApiConversationsRepliesResponse: Encoder.AsObject[SlackApiConversationsRepliesResponse] =
      deriveEncoder[SlackApiConversationsRepliesResponse]

    implicit val decoderSlackApiConversationsRepliesResponse: Decoder[SlackApiConversationsRepliesResponse] =
      deriveDecoder[SlackApiConversationsRepliesResponse]

    implicit val encoderSlackApiConversationsSetPurposeRequest
        : Encoder.AsObject[SlackApiConversationsSetPurposeRequest] =
      deriveEncoder[SlackApiConversationsSetPurposeRequest]

    implicit val decoderSlackApiConversationsSetPurposeRequest: Decoder[SlackApiConversationsSetPurposeRequest] =
      deriveDecoder[SlackApiConversationsSetPurposeRequest]

    implicit val encoderSlackApiConversationsSetPurposeResponse
        : Encoder.AsObject[SlackApiConversationsSetPurposeResponse] =
      deriveEncoder[SlackApiConversationsSetPurposeResponse]

    implicit val decoderSlackApiConversationsSetPurposeResponse: Decoder[SlackApiConversationsSetPurposeResponse] =
      deriveDecoder[SlackApiConversationsSetPurposeResponse]

    implicit val encoderSlackApiConversationsSetTopicRequest: Encoder.AsObject[SlackApiConversationsSetTopicRequest] =
      deriveEncoder[SlackApiConversationsSetTopicRequest]

    implicit val decoderSlackApiConversationsSetTopicRequest: Decoder[SlackApiConversationsSetTopicRequest] =
      deriveDecoder[SlackApiConversationsSetTopicRequest]

    implicit val encoderSlackApiConversationsSetTopicResponse: Encoder.AsObject[SlackApiConversationsSetTopicResponse] =
      deriveEncoder[SlackApiConversationsSetTopicResponse]

    implicit val decoderSlackApiConversationsSetTopicResponse: Decoder[SlackApiConversationsSetTopicResponse] =
      deriveDecoder[SlackApiConversationsSetTopicResponse]

    implicit val encoderSlackApiConversationsUnarchiveRequest: Encoder.AsObject[SlackApiConversationsUnarchiveRequest] =
      deriveEncoder[SlackApiConversationsUnarchiveRequest]

    implicit val decoderSlackApiConversationsUnarchiveRequest: Decoder[SlackApiConversationsUnarchiveRequest] =
      deriveDecoder[SlackApiConversationsUnarchiveRequest]

    implicit val encoderSlackApiConversationsUnarchiveResponse
        : Encoder.AsObject[SlackApiConversationsUnarchiveResponse] =
      deriveEncoder[SlackApiConversationsUnarchiveResponse]

    implicit val decoderSlackApiConversationsUnarchiveResponse: Decoder[SlackApiConversationsUnarchiveResponse] =
      deriveDecoder[SlackApiConversationsUnarchiveResponse]

    implicit val encoderSlackApiDndEndDndRequest: Encoder.AsObject[SlackApiDndEndDndRequest] =
      deriveEncoder[SlackApiDndEndDndRequest]

    implicit val decoderSlackApiDndEndDndRequest: Decoder[SlackApiDndEndDndRequest] =
      deriveDecoder[SlackApiDndEndDndRequest]

    implicit val encoderSlackApiDndEndDndResponse: Encoder.AsObject[SlackApiDndEndDndResponse] =
      deriveEncoder[SlackApiDndEndDndResponse]

    implicit val decoderSlackApiDndEndDndResponse: Decoder[SlackApiDndEndDndResponse] =
      deriveDecoder[SlackApiDndEndDndResponse]

    implicit val encoderSlackApiDndEndSnoozeRequest: Encoder.AsObject[SlackApiDndEndSnoozeRequest] =
      deriveEncoder[SlackApiDndEndSnoozeRequest]

    implicit val decoderSlackApiDndEndSnoozeRequest: Decoder[SlackApiDndEndSnoozeRequest] =
      deriveDecoder[SlackApiDndEndSnoozeRequest]

    implicit val encoderSlackApiDndEndSnoozeResponse: Encoder.AsObject[SlackApiDndEndSnoozeResponse] =
      deriveEncoder[SlackApiDndEndSnoozeResponse]

    implicit val decoderSlackApiDndEndSnoozeResponse: Decoder[SlackApiDndEndSnoozeResponse] =
      deriveDecoder[SlackApiDndEndSnoozeResponse]

    implicit val encoderSlackApiDndInfoRequest: Encoder.AsObject[SlackApiDndInfoRequest] =
      deriveEncoder[SlackApiDndInfoRequest]
    implicit val decoderSlackApiDndInfoRequest: Decoder[SlackApiDndInfoRequest] = deriveDecoder[SlackApiDndInfoRequest]

    implicit val encoderSlackApiDndInfoResponse: Encoder.AsObject[SlackApiDndInfoResponse] =
      deriveEncoder[SlackApiDndInfoResponse]

    implicit val decoderSlackApiDndInfoResponse: Decoder[SlackApiDndInfoResponse] =
      deriveDecoder[SlackApiDndInfoResponse]

    implicit val encoderSlackApiDndSetSnoozeRequest: Encoder.AsObject[SlackApiDndSetSnoozeRequest] =
      deriveEncoder[SlackApiDndSetSnoozeRequest]

    implicit val decoderSlackApiDndSetSnoozeRequest: Decoder[SlackApiDndSetSnoozeRequest] =
      deriveDecoder[SlackApiDndSetSnoozeRequest]

    implicit val encoderSlackApiDndSetSnoozeResponse: Encoder.AsObject[SlackApiDndSetSnoozeResponse] =
      deriveEncoder[SlackApiDndSetSnoozeResponse]

    implicit val decoderSlackApiDndSetSnoozeResponse: Decoder[SlackApiDndSetSnoozeResponse] =
      deriveDecoder[SlackApiDndSetSnoozeResponse]

    implicit val encoderSlackApiDndTeamInfoRequest: Encoder.AsObject[SlackApiDndTeamInfoRequest] =
      deriveEncoder[SlackApiDndTeamInfoRequest]

    implicit val decoderSlackApiDndTeamInfoRequest: Decoder[SlackApiDndTeamInfoRequest] =
      deriveDecoder[SlackApiDndTeamInfoRequest]

    implicit val encoderSlackApiDndUserInfo: Encoder.AsObject[SlackApiDndUserInfo] = deriveEncoder[SlackApiDndUserInfo]
    implicit val decoderSlackApiDndUserInfo: Decoder[SlackApiDndUserInfo] = deriveDecoder[SlackApiDndUserInfo]

    implicit val encoderSlackApiDndTeamInfoResponse: Encoder.AsObject[SlackApiDndTeamInfoResponse] =
      deriveEncoder[SlackApiDndTeamInfoResponse]

    implicit val decoderSlackApiDndTeamInfoResponse: Decoder[SlackApiDndTeamInfoResponse] =
      deriveDecoder[SlackApiDndTeamInfoResponse]

    implicit val encoderSlackApiEmojiListRequest: Encoder.AsObject[SlackApiEmojiListRequest] =
      deriveEncoder[SlackApiEmojiListRequest]

    implicit val decoderSlackApiEmojiListRequest: Decoder[SlackApiEmojiListRequest] =
      deriveDecoder[SlackApiEmojiListRequest]

    implicit val encoderSlackApiEmojiListResponse: Encoder.AsObject[SlackApiEmojiListResponse] =
      deriveEncoder[SlackApiEmojiListResponse]

    implicit val decoderSlackApiEmojiListResponse: Decoder[SlackApiEmojiListResponse] =
      deriveDecoder[SlackApiEmojiListResponse]

    implicit val encoderSlackApiImCloseRequest: Encoder.AsObject[SlackApiImCloseRequest] =
      deriveEncoder[SlackApiImCloseRequest]
    implicit val decoderSlackApiImCloseRequest: Decoder[SlackApiImCloseRequest] = deriveDecoder[SlackApiImCloseRequest]

    implicit val encoderSlackApiImCloseResponse: Encoder.AsObject[SlackApiImCloseResponse] =
      deriveEncoder[SlackApiImCloseResponse]

    implicit val decoderSlackApiImCloseResponse: Decoder[SlackApiImCloseResponse] =
      deriveDecoder[SlackApiImCloseResponse]

    implicit val encoderSlackApiImHistoryRequest: Encoder.AsObject[SlackApiImHistoryRequest] =
      deriveEncoder[SlackApiImHistoryRequest]

    implicit val decoderSlackApiImHistoryRequest: Decoder[SlackApiImHistoryRequest] =
      deriveDecoder[SlackApiImHistoryRequest]

    implicit val encoderSlackApiImHistoryResponse: Encoder.AsObject[SlackApiImHistoryResponse] =
      deriveEncoder[SlackApiImHistoryResponse]

    implicit val decoderSlackApiImHistoryResponse: Decoder[SlackApiImHistoryResponse] =
      deriveDecoder[SlackApiImHistoryResponse]

    implicit val encoderSlackApiImListRequest: Encoder.AsObject[SlackApiImListRequest] =
      deriveEncoder[SlackApiImListRequest]
    implicit val decoderSlackApiImListRequest: Decoder[SlackApiImListRequest] = deriveDecoder[SlackApiImListRequest]

    implicit val encoderSlackApiImListResponse: Encoder.AsObject[SlackApiImListResponse] =
      deriveEncoder[SlackApiImListResponse]
    implicit val decoderSlackApiImListResponse: Decoder[SlackApiImListResponse] = deriveDecoder[SlackApiImListResponse]

    implicit val encoderSlackApiImMarkRequest: Encoder.AsObject[SlackApiImMarkRequest] =
      deriveEncoder[SlackApiImMarkRequest]
    implicit val decoderSlackApiImMarkRequest: Decoder[SlackApiImMarkRequest] = deriveDecoder[SlackApiImMarkRequest]

    implicit val encoderSlackApiImMarkResponse: Encoder.AsObject[SlackApiImMarkResponse] =
      deriveEncoder[SlackApiImMarkResponse]
    implicit val decoderSlackApiImMarkResponse: Decoder[SlackApiImMarkResponse] = deriveDecoder[SlackApiImMarkResponse]

    implicit val encoderSlackApiImOpenRequest: Encoder.AsObject[SlackApiImOpenRequest] =
      deriveEncoder[SlackApiImOpenRequest]
    implicit val decoderSlackApiImOpenRequest: Decoder[SlackApiImOpenRequest] = deriveDecoder[SlackApiImOpenRequest]

    implicit val encoderSlackApiImOpenResponse: Encoder.AsObject[SlackApiImOpenResponse] =
      deriveEncoder[SlackApiImOpenResponse]
    implicit val decoderSlackApiImOpenResponse: Decoder[SlackApiImOpenResponse] = deriveDecoder[SlackApiImOpenResponse]

    implicit val encoderSlackApiImRepliesRequest: Encoder.AsObject[SlackApiImRepliesRequest] =
      deriveEncoder[SlackApiImRepliesRequest]

    implicit val decoderSlackApiImRepliesRequest: Decoder[SlackApiImRepliesRequest] =
      deriveDecoder[SlackApiImRepliesRequest]

    implicit val encoderSlackApiImRepliesResponse: Encoder.AsObject[SlackApiImRepliesResponse] =
      deriveEncoder[SlackApiImRepliesResponse]

    implicit val decoderSlackApiImRepliesResponse: Decoder[SlackApiImRepliesResponse] =
      deriveDecoder[SlackApiImRepliesResponse]

    implicit val encoderSlackInteractionResponse: Encoder.AsObject[SlackInteractionResponse] =
      deriveEncoder[SlackInteractionResponse]

    implicit val decoderSlackInteractionResponse: Decoder[SlackInteractionResponse] =
      deriveDecoder[SlackInteractionResponse]

    implicit val encoderSlackOAuthV1BotToken: Encoder.AsObject[SlackOAuthV1BotToken] =
      deriveEncoder[SlackOAuthV1BotToken]
    implicit val decoderSlackOAuthV1BotToken: Decoder[SlackOAuthV1BotToken] = deriveDecoder[SlackOAuthV1BotToken]

    implicit val encoderSlackOAuthIncomingWebHook: Encoder.AsObject[SlackOAuthIncomingWebHook] =
      deriveEncoder[SlackOAuthIncomingWebHook]

    implicit val decoderSlackOAuthIncomingWebHook: Decoder[SlackOAuthIncomingWebHook] =
      deriveDecoder[SlackOAuthIncomingWebHook]

    implicit val encoderSlackOAuthV1AccessTokenResponse: Encoder.AsObject[SlackOAuthV1AccessTokenResponse] =
      deriveEncoder[SlackOAuthV1AccessTokenResponse]

    implicit val decoderSlackOAuthV1AccessTokenResponse: Decoder[SlackOAuthV1AccessTokenResponse] =
      deriveDecoder[SlackOAuthV1AccessTokenResponse]

    implicit val encoderSlackOAuthV2AuthedUser: Encoder.AsObject[SlackOAuthV2AuthedUser] =
      deriveEncoder[SlackOAuthV2AuthedUser]
    implicit val decoderSlackOAuthV2AuthedUser: Decoder[SlackOAuthV2AuthedUser] = deriveDecoder[SlackOAuthV2AuthedUser]

    implicit val encoderSlackOAuthV2AccessTokenResponse: Encoder.AsObject[SlackOAuthV2AccessTokenResponse] =
      deriveEncoder[SlackOAuthV2AccessTokenResponse]

    implicit val decoderSlackOAuthV2AccessTokenResponse: Decoder[SlackOAuthV2AccessTokenResponse] =
      deriveDecoder[SlackOAuthV2AccessTokenResponse]

    implicit val encoderSlackApiPinsAddRequest: Encoder.AsObject[SlackApiPinsAddRequest] =
      deriveEncoder[SlackApiPinsAddRequest]
    implicit val decoderSlackApiPinsAddRequest: Decoder[SlackApiPinsAddRequest] = deriveDecoder[SlackApiPinsAddRequest]

    implicit val encoderSlackApiPinsAddResponse: Encoder.AsObject[SlackApiPinsAddResponse] =
      deriveEncoder[SlackApiPinsAddResponse]

    implicit val decoderSlackApiPinsAddResponse: Decoder[SlackApiPinsAddResponse] =
      deriveDecoder[SlackApiPinsAddResponse]

    implicit val encoderSlackApiPinsListRequest: Encoder.AsObject[SlackApiPinsListRequest] =
      deriveEncoder[SlackApiPinsListRequest]

    implicit val decoderSlackApiPinsListRequest: Decoder[SlackApiPinsListRequest] =
      deriveDecoder[SlackApiPinsListRequest]

    implicit val encoderSlackApiPinsListResponse: Encoder.AsObject[SlackApiPinsListResponse] =
      deriveEncoder[SlackApiPinsListResponse]

    implicit val decoderSlackApiPinsListResponse: Decoder[SlackApiPinsListResponse] =
      deriveDecoder[SlackApiPinsListResponse]

    implicit val encoderSlackApiPinsRemoveRequest: Encoder.AsObject[SlackApiPinsRemoveRequest] =
      deriveEncoder[SlackApiPinsRemoveRequest]

    implicit val decoderSlackApiPinsRemoveRequest: Decoder[SlackApiPinsRemoveRequest] =
      deriveDecoder[SlackApiPinsRemoveRequest]

    implicit val encoderSlackApiPinsRemoveResponse: Encoder.AsObject[SlackApiPinsRemoveResponse] =
      deriveEncoder[SlackApiPinsRemoveResponse]

    implicit val decoderSlackApiPinsRemoveResponse: Decoder[SlackApiPinsRemoveResponse] =
      deriveDecoder[SlackApiPinsRemoveResponse]

    implicit val encoderSlackApiReactionsAddRequest: Encoder.AsObject[SlackApiReactionsAddRequest] =
      deriveEncoder[SlackApiReactionsAddRequest]

    implicit val decoderSlackApiReactionsAddRequest: Decoder[SlackApiReactionsAddRequest] =
      deriveDecoder[SlackApiReactionsAddRequest]

    implicit val encoderSlackApiReactionsAddResponse: Encoder.AsObject[SlackApiReactionsAddResponse] =
      deriveEncoder[SlackApiReactionsAddResponse]

    implicit val decoderSlackApiReactionsAddResponse: Decoder[SlackApiReactionsAddResponse] =
      deriveDecoder[SlackApiReactionsAddResponse]

    implicit val encoderSlackApiReactionsGetRequest: Encoder.AsObject[SlackApiReactionsGetRequest] =
      deriveEncoder[SlackApiReactionsGetRequest]

    implicit val decoderSlackApiReactionsGetRequest: Decoder[SlackApiReactionsGetRequest] =
      deriveDecoder[SlackApiReactionsGetRequest]

    implicit val encoderSlackApiReactionsGetResponse: Encoder.AsObject[SlackApiReactionsGetResponse] =
      deriveEncoder[SlackApiReactionsGetResponse]

    implicit val decoderSlackApiReactionsGetResponse: Decoder[SlackApiReactionsGetResponse] =
      deriveDecoder[SlackApiReactionsGetResponse]

    implicit val encoderSlackApiReactionsListRequest: Encoder.AsObject[SlackApiReactionsListRequest] =
      deriveEncoder[SlackApiReactionsListRequest]

    implicit val encoderSlackApiReactionsListItem: Encoder.AsObject[SlackApiReactionsListItem] =
      deriveEncoder[SlackApiReactionsListItem]

    implicit val decoderSlackApiReactionsListItem: Decoder[SlackApiReactionsListItem] =
      deriveDecoder[SlackApiReactionsListItem]

    implicit val decoderSlackApiReactionsListRequest: Decoder[SlackApiReactionsListRequest] =
      deriveDecoder[SlackApiReactionsListRequest]

    implicit val encoderSlackApiReactionsListResponse: Encoder.AsObject[SlackApiReactionsListResponse] =
      deriveEncoder[SlackApiReactionsListResponse]

    implicit val decoderSlackApiReactionsListResponse: Decoder[SlackApiReactionsListResponse] =
      deriveDecoder[SlackApiReactionsListResponse]

    implicit val encoderSlackApiReactionsRemoveRequest: Encoder.AsObject[SlackApiReactionsRemoveRequest] =
      deriveEncoder[SlackApiReactionsRemoveRequest]

    implicit val decoderSlackApiReactionsRemoveRequest: Decoder[SlackApiReactionsRemoveRequest] =
      deriveDecoder[SlackApiReactionsRemoveRequest]

    implicit val encoderSlackApiReactionsRemoveResponse: Encoder.AsObject[SlackApiReactionsRemoveResponse] =
      deriveEncoder[SlackApiReactionsRemoveResponse]

    implicit val decoderSlackApiReactionsRemoveResponse: Decoder[SlackApiReactionsRemoveResponse] =
      deriveDecoder[SlackApiReactionsRemoveResponse]

    implicit val encoderSlackApiTeamInfoRequest: Encoder.AsObject[SlackApiTeamInfoRequest] =
      deriveEncoder[SlackApiTeamInfoRequest]

    implicit val decoderSlackApiTeamInfoRequest: Decoder[SlackApiTeamInfoRequest] =
      deriveDecoder[SlackApiTeamInfoRequest]

    implicit val encoderSlackApiTeamInfoResponse: Encoder.AsObject[SlackApiTeamInfoResponse] =
      deriveEncoder[SlackApiTeamInfoResponse]

    implicit val decoderSlackApiTeamInfoResponse: Decoder[SlackApiTeamInfoResponse] =
      deriveDecoder[SlackApiTeamInfoResponse]

    implicit val encoderSlackApiTeamProfileGetRequest: Encoder.AsObject[SlackApiTeamProfileGetRequest] =
      deriveEncoder[SlackApiTeamProfileGetRequest]

    implicit val decoderSlackApiTeamProfileGetRequest: Decoder[SlackApiTeamProfileGetRequest] =
      deriveDecoder[SlackApiTeamProfileGetRequest]

    implicit val encoderSlackApiTeamProfileGetResponse: Encoder.AsObject[SlackApiTeamProfileGetResponse] =
      deriveEncoder[SlackApiTeamProfileGetResponse]

    implicit val decoderSlackApiTeamProfileGetResponse: Decoder[SlackApiTeamProfileGetResponse] =
      deriveDecoder[SlackApiTeamProfileGetResponse]

    implicit val encoderSlackApiTestRequest: Encoder.AsObject[SlackApiTestRequest] = deriveEncoder[SlackApiTestRequest]
    implicit val decoderSlackApiTestRequest: Decoder[SlackApiTestRequest] = deriveDecoder[SlackApiTestRequest]

    implicit val encoderSlackApiTestResponse: Encoder.AsObject[SlackApiTestResponse] =
      deriveEncoder[SlackApiTestResponse]
    implicit val decoderSlackApiTestResponse: Decoder[SlackApiTestResponse] = deriveDecoder[SlackApiTestResponse]

    implicit val encoderSlackApiUsersConversationsRequest: Encoder.AsObject[SlackApiUsersConversationsRequest] =
      deriveEncoder[SlackApiUsersConversationsRequest]

    implicit val decoderSlackApiUsersConversationsRequest: Decoder[SlackApiUsersConversationsRequest] =
      deriveDecoder[SlackApiUsersConversationsRequest]

    implicit val encoderSlackApiUsersConversationsResponse: Encoder.AsObject[SlackApiUsersConversationsResponse] =
      deriveEncoder[SlackApiUsersConversationsResponse]

    implicit val decoderSlackApiUsersConversationsResponse: Decoder[SlackApiUsersConversationsResponse] =
      deriveDecoder[SlackApiUsersConversationsResponse]

    implicit val encoderSlackApiUsersGetPresenceRequest: Encoder.AsObject[SlackApiUsersGetPresenceRequest] =
      deriveEncoder[SlackApiUsersGetPresenceRequest]

    implicit val decoderSlackApiUsersGetPresenceRequest: Decoder[SlackApiUsersGetPresenceRequest] =
      deriveDecoder[SlackApiUsersGetPresenceRequest]

    implicit val encoderSlackApiUsersGetPresenceResponse: Encoder.AsObject[SlackApiUsersGetPresenceResponse] =
      deriveEncoder[SlackApiUsersGetPresenceResponse]

    implicit val decoderSlackApiUsersGetPresenceResponse: Decoder[SlackApiUsersGetPresenceResponse] =
      deriveDecoder[SlackApiUsersGetPresenceResponse]

    implicit val encoderSlackApiUsersIdentityRequest: Encoder.AsObject[SlackApiUsersIdentityRequest] =
      deriveEncoder[SlackApiUsersIdentityRequest]

    implicit val decoderSlackApiUsersIdentityRequest: Decoder[SlackApiUsersIdentityRequest] =
      deriveDecoder[SlackApiUsersIdentityRequest]

    implicit val encoderSlackApiUsersIdentityResponse: Encoder.AsObject[SlackApiUsersIdentityResponse] =
      deriveEncoder[SlackApiUsersIdentityResponse]

    implicit val decoderSlackApiUsersIdentityResponse: Decoder[SlackApiUsersIdentityResponse] =
      deriveDecoder[SlackApiUsersIdentityResponse]

    implicit val encoderSlackApiUsersInfoRequest: Encoder.AsObject[SlackApiUsersInfoRequest] =
      deriveEncoder[SlackApiUsersInfoRequest]

    implicit val decoderSlackApiUsersInfoRequest: Decoder[SlackApiUsersInfoRequest] =
      deriveDecoder[SlackApiUsersInfoRequest]

    implicit val encoderSlackApiUsersInfoResponse: Encoder.AsObject[SlackApiUsersInfoResponse] =
      deriveEncoder[SlackApiUsersInfoResponse]

    implicit val decoderSlackApiUsersInfoResponse: Decoder[SlackApiUsersInfoResponse] =
      deriveDecoder[SlackApiUsersInfoResponse]

    implicit val encoderSlackApiUsersListRequest: Encoder.AsObject[SlackApiUsersListRequest] =
      deriveEncoder[SlackApiUsersListRequest]

    implicit val decoderSlackApiUsersListRequest: Decoder[SlackApiUsersListRequest] =
      deriveDecoder[SlackApiUsersListRequest]

    implicit val encoderSlackApiUsersListResponse: Encoder.AsObject[SlackApiUsersListResponse] =
      deriveEncoder[SlackApiUsersListResponse]

    implicit val decoderSlackApiUsersListResponse: Decoder[SlackApiUsersListResponse] =
      deriveDecoder[SlackApiUsersListResponse]

    implicit val encoderSlackApiUsersLookupByEmailRequest: Encoder.AsObject[SlackApiUsersLookupByEmailRequest] =
      deriveEncoder[SlackApiUsersLookupByEmailRequest]

    implicit val decoderSlackApiUsersLookupByEmailRequest: Decoder[SlackApiUsersLookupByEmailRequest] =
      deriveDecoder[SlackApiUsersLookupByEmailRequest]

    implicit val encoderSlackApiUsersLookupByEmailResponse: Encoder.AsObject[SlackApiUsersLookupByEmailResponse] =
      deriveEncoder[SlackApiUsersLookupByEmailResponse]

    implicit val decoderSlackApiUsersLookupByEmailResponse: Decoder[SlackApiUsersLookupByEmailResponse] =
      deriveDecoder[SlackApiUsersLookupByEmailResponse]

    implicit val encoderSlackApiUsersProfileGetRequest: Encoder.AsObject[SlackApiUsersProfileGetRequest] =
      deriveEncoder[SlackApiUsersProfileGetRequest]

    implicit val decoderSlackApiUsersProfileGetRequest: Decoder[SlackApiUsersProfileGetRequest] =
      deriveDecoder[SlackApiUsersProfileGetRequest]

    implicit val encoderSlackApiUsersProfileGetResponse: Encoder.AsObject[SlackApiUsersProfileGetResponse] =
      deriveEncoder[SlackApiUsersProfileGetResponse]

    implicit val decoderSlackApiUsersProfileGetResponse: Decoder[SlackApiUsersProfileGetResponse] =
      deriveDecoder[SlackApiUsersProfileGetResponse]

    implicit val encoderSlackApiUsersProfileSetRequest: Encoder.AsObject[SlackApiUsersProfileSetRequest] =
      deriveEncoder[SlackApiUsersProfileSetRequest]

    implicit val decoderSlackApiUsersProfileSetRequest: Decoder[SlackApiUsersProfileSetRequest] =
      deriveDecoder[SlackApiUsersProfileSetRequest]

    implicit val encoderSlackApiUsersProfileSetResponse: Encoder.AsObject[SlackApiUsersProfileSetResponse] =
      deriveEncoder[SlackApiUsersProfileSetResponse]

    implicit val decoderSlackApiUsersProfileSetResponse: Decoder[SlackApiUsersProfileSetResponse] =
      deriveDecoder[SlackApiUsersProfileSetResponse]

    implicit val encoderSlackApiUsersSetPresenceRequest: Encoder.AsObject[SlackApiUsersSetPresenceRequest] =
      deriveEncoder[SlackApiUsersSetPresenceRequest]

    implicit val decoderSlackApiUsersSetPresenceRequest: Decoder[SlackApiUsersSetPresenceRequest] =
      deriveDecoder[SlackApiUsersSetPresenceRequest]

    implicit val encoderSlackApiUsersSetPresenceResponse: Encoder.AsObject[SlackApiUsersSetPresenceResponse] =
      deriveEncoder[SlackApiUsersSetPresenceResponse]

    implicit val decoderSlackApiUsersSetPresenceResponse: Decoder[SlackApiUsersSetPresenceResponse] =
      deriveDecoder[SlackApiUsersSetPresenceResponse]

    implicit val encoderSlackApiViewsOpenRequest: Encoder.AsObject[SlackApiViewsOpenRequest] =
      deriveEncoder[SlackApiViewsOpenRequest]

    implicit val decoderSlackApiViewsOpenRequest: Decoder[SlackApiViewsOpenRequest] =
      deriveDecoder[SlackApiViewsOpenRequest]

    implicit val encoderSlackApiViewsOpenResponse: Encoder.AsObject[SlackApiViewsOpenResponse] =
      deriveEncoder[SlackApiViewsOpenResponse]

    implicit val decoderSlackApiViewsOpenResponse: Decoder[SlackApiViewsOpenResponse] =
      deriveDecoder[SlackApiViewsOpenResponse]

    implicit val encoderSlackApiViewsPublishRequest: Encoder.AsObject[SlackApiViewsPublishRequest] =
      deriveEncoder[SlackApiViewsPublishRequest]

    implicit val decoderSlackApiViewsPublishRequest: Decoder[SlackApiViewsPublishRequest] =
      deriveDecoder[SlackApiViewsPublishRequest]

    implicit val encoderSlackApiViewsPublishResponse: Encoder.AsObject[SlackApiViewsPublishResponse] =
      deriveEncoder[SlackApiViewsPublishResponse]

    implicit val decoderSlackApiViewsPublishResponse: Decoder[SlackApiViewsPublishResponse] =
      deriveDecoder[SlackApiViewsPublishResponse]

    implicit val encoderSlackApiViewsPushRequest: Encoder.AsObject[SlackApiViewsPushRequest] =
      deriveEncoder[SlackApiViewsPushRequest]

    implicit val decoderSlackApiViewsPushRequest: Decoder[SlackApiViewsPushRequest] =
      deriveDecoder[SlackApiViewsPushRequest]

    implicit val encoderSlackApiViewsPushResponse: Encoder.AsObject[SlackApiViewsPushResponse] =
      deriveEncoder[SlackApiViewsPushResponse]

    implicit val decoderSlackApiViewsPushResponse: Decoder[SlackApiViewsPushResponse] =
      deriveDecoder[SlackApiViewsPushResponse]

    implicit val encoderSlackApiViewsUpdateRequest: Encoder.AsObject[SlackApiViewsUpdateRequest] =
      deriveEncoder[SlackApiViewsUpdateRequest]

    implicit val decoderSlackApiViewsUpdateRequest: Decoder[SlackApiViewsUpdateRequest] =
      deriveDecoder[SlackApiViewsUpdateRequest]

    implicit val encoderSlackApiViewsUpdateResponse: Encoder.AsObject[SlackApiViewsUpdateResponse] =
      deriveEncoder[SlackApiViewsUpdateResponse]

    implicit val decoderSlackApiViewsUpdateResponse: Decoder[SlackApiViewsUpdateResponse] =
      deriveDecoder[SlackApiViewsUpdateResponse]
  }

  object implicits extends CirceCodecs {}

}
