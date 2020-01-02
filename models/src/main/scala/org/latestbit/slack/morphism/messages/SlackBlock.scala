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

package org.latestbit.slack.morphism.messages

import cats.data.NonEmptyList
import io.circe._
import io.circe.generic.semiauto._
import org.latestbit.circe.adt.codec._

/**
 * Blocks are a series of components that can be combined to create visually rich and compellingly interactive messages and views.
 * https://api.slack.com/reference/block-kit/blocks
 */
sealed trait SlackBlock {
  val block_id: Option[String]
}

object SlackBlock {
  import SlackBlockText._
  import SlackBlockPlainText._
  import SlackBlockMarkDownText._

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

  implicit val encoder: Encoder[SlackBlock] = JsonTaggedAdtCodec.createEncoder[SlackBlock]( "type" )
  implicit val decoder: Decoder[SlackBlock] = JsonTaggedAdtCodec.createDecoder[SlackBlock]( "type" )
}

object SlackBlockTextTypes {
  final val MARK_DOWN = "mrkdwn"
  final val PLAIN_TEXT = "plain_text"
}

sealed trait SlackBlockText extends SlackBlockElement

object SlackBlockText {

  def createEncoder() = {
    implicit val encoderSlackBlockPlainText: Encoder.AsObject[SlackBlockPlainText] = deriveEncoder[SlackBlockPlainText]
    implicit val encoderSlackMarkDownText: Encoder.AsObject[SlackBlockMarkDownText] =
      deriveEncoder[SlackBlockMarkDownText]

    implicit val decoderSlackBlockPlainText: Decoder[SlackBlockPlainText] = deriveDecoder[SlackBlockPlainText]
    implicit val decoderSlackMarkDownText: Decoder[SlackBlockMarkDownText] = deriveDecoder[SlackBlockMarkDownText]

    JsonTaggedAdtCodec.createEncoder[SlackBlockText]( "type" )
  }

  def createDecoder() = {
    implicit val encoderSlackBlockPlainText: Encoder.AsObject[SlackBlockPlainText] = deriveEncoder[SlackBlockPlainText]
    implicit val encoderSlackMarkDownText: Encoder.AsObject[SlackBlockMarkDownText] =
      deriveEncoder[SlackBlockMarkDownText]

    implicit val decoderSlackBlockPlainText: Decoder[SlackBlockPlainText] = deriveDecoder[SlackBlockPlainText]
    implicit val decoderSlackMarkDownText: Decoder[SlackBlockMarkDownText] = deriveDecoder[SlackBlockMarkDownText]

    JsonTaggedAdtCodec.createDecoder[SlackBlockText]( "type" )
  }

  implicit val encoder: Encoder.AsObject[SlackBlockText] = createEncoder()
  implicit val decoder: Decoder[SlackBlockText] = createDecoder()
}

@JsonAdt( SlackBlockTextTypes.PLAIN_TEXT )
case class SlackBlockPlainText(
    text: String,
    emoji: Option[Boolean] = None
) extends SlackBlockText

object SlackBlockPlainText {
  implicit val encoderSlackBlockPlainText: Encoder.AsObject[SlackBlockPlainText] = deriveEncoder[SlackBlockPlainText]
  implicit val encoder: Encoder[SlackBlockPlainText] = JsonTaggedAdtCodec.createEncoder[SlackBlockPlainText]( "type" )
  implicit val decoder: Decoder[SlackBlockPlainText] = JsonTaggedAdtCodec.createDecoder[SlackBlockPlainText]( "type" )
}

@JsonAdt( SlackBlockTextTypes.MARK_DOWN )
case class SlackBlockMarkDownText(
    text: String,
    verbatim: Option[Boolean] = None
) extends SlackBlockText

object SlackBlockMarkDownText {

  implicit val encoderSlackMarkDownText: Encoder.AsObject[SlackBlockMarkDownText] =
    deriveEncoder[SlackBlockMarkDownText]

  implicit val encoder: Encoder[SlackBlockMarkDownText] =
    JsonTaggedAdtCodec.createEncoder[SlackBlockMarkDownText]( "type" )

  implicit val decoder: Decoder[SlackBlockMarkDownText] =
    JsonTaggedAdtCodec.createDecoder[SlackBlockMarkDownText]( "type" )
}

@JsonAdt( "section" )
case class SlackSectionBlock(
    text: SlackBlockText,
    override val block_id: Option[String] = None,
    fields: Option[List[SlackBlockText]] = None,
    accessory: Option[SlackBlockElement] = None
) extends SlackBlock

@JsonAdt( "divider" )
case class SlackDividerBlock( override val block_id: Option[String] = None ) extends SlackBlock

@JsonAdt( "image" )
case class SlackImageBlock(
    image_url: String,
    alt_text: String,
    title: Option[SlackBlockPlainText] = None,
    override val block_id: Option[String] = None
) extends SlackBlock

@JsonAdt( "actions" )
case class SlackActionsBlock( elements: List[SlackBlockElement], override val block_id: Option[String] = None )
    extends SlackBlock

@JsonAdt( "context" )
case class SlackContextBlock( elements: NonEmptyList[SlackBlockElement], override val block_id: Option[String] = None )
    extends SlackBlock

@JsonAdt( "input" )
case class SlackInputBlock(
    label: SlackBlockPlainText,
    element: SlackBlockElement,
    hint: Option[SlackBlockPlainText] = None,
    optional: Option[Boolean] = None,
    override val block_id: Option[String] = None
) extends SlackBlock

@JsonAdt( "file" )
case class SlackFileBlock(
    external_id: String,
    source: String = "remote",
    override val block_id: Option[String] = None
) extends SlackBlock

@JsonAdt( "rich_text" )
case class SlackRichTextBlock( elements: List[SlackBlockElement], override val block_id: Option[String] = None )
    extends SlackBlock

/**
 * Block elements can be used inside of section, context, and actions layout blocks. Inputs can only be used inside of input blocks.
 * https://api.slack.com/reference/block-kit/block-elements
 */
sealed trait SlackBlockElement

object SlackBlockElement {
  import SlackBlockText._
  import SlackBlockPlainText._
  import SlackBlockMarkDownText._

  implicit val encoderSlackBlockConfirmItem: Encoder.AsObject[SlackBlockConfirmItem] =
    deriveEncoder[SlackBlockConfirmItem]
  implicit val decoderSlackBlockConfirmItem: Decoder[SlackBlockConfirmItem] = deriveDecoder[SlackBlockConfirmItem]

  implicit val encoderSlackBlockButtonElement: Encoder.AsObject[SlackBlockButtonElement] =
    deriveEncoder[SlackBlockButtonElement]
  implicit val decoderSlackBlockButtonElement: Decoder[SlackBlockButtonElement] = deriveDecoder[SlackBlockButtonElement]

  implicit val encoderSlackBlockRichTextList: Encoder.AsObject[SlackBlockRichTextList] =
    deriveEncoder[SlackBlockRichTextList]
  implicit val decoderSlackBlockRichTextList: Decoder[SlackBlockRichTextList] = deriveDecoder[SlackBlockRichTextList]

  implicit val encoderSlackBlockOptionItem: Encoder.AsObject[SlackBlockOptionItem] = deriveEncoder[SlackBlockOptionItem]
  implicit val decoderSlackBlockOptionItem: Decoder[SlackBlockOptionItem] = deriveDecoder[SlackBlockOptionItem]

  implicit val encoderSlackBlockOptionGroup: Encoder.AsObject[SlackBlockOptionGroup] =
    deriveEncoder[SlackBlockOptionGroup]
  implicit val decoderSlackBlockOptionGroup: Decoder[SlackBlockOptionGroup] = deriveDecoder[SlackBlockOptionGroup]

  implicit val encoderSlackBlockMenuElement: Encoder.AsObject[SlackBlockMenuElement] =
    deriveEncoder[SlackBlockMenuElement]
  implicit val decoderSlackBlockMenuElement: Decoder[SlackBlockMenuElement] = deriveDecoder[SlackBlockMenuElement]

  implicit val encoderSlackBlockImageElement: Encoder.AsObject[SlackBlockImageElement] =
    deriveEncoder[SlackBlockImageElement]
  implicit val decoderSlackBlockImageElement: Decoder[SlackBlockImageElement] = deriveDecoder[SlackBlockImageElement]

  implicit val encoderSlackBlockStaticMenuElement: Encoder.AsObject[SlackBlockStaticMenuElement] =
    deriveEncoder[SlackBlockStaticMenuElement]

  implicit val decoderSlackBlockStaticMenuElement: Decoder[SlackBlockStaticMenuElement] =
    deriveDecoder[SlackBlockStaticMenuElement]

  implicit val encoderSlackBlockRichTextSection: Encoder.AsObject[SlackBlockRichTextSection] =
    deriveEncoder[SlackBlockRichTextSection]

  implicit val decoderSlackBlockRichTextSection: Decoder[SlackBlockRichTextSection] =
    deriveDecoder[SlackBlockRichTextSection]

  implicit val encoderSlackBlockRichTextPreformatted: Encoder.AsObject[SlackBlockRichTextPreformatted] =
    deriveEncoder[SlackBlockRichTextPreformatted]

  implicit val decoderSlackBlockRichTextPreformatted: Decoder[SlackBlockRichTextPreformatted] =
    deriveDecoder[SlackBlockRichTextPreformatted]

  implicit val encoderSlackBlockConversationListMenuElement: Encoder.AsObject[SlackBlockConversationListMenuElement] =
    deriveEncoder[SlackBlockConversationListMenuElement]

  implicit val decoderSlackBlockConversationListMenuElement: Decoder[SlackBlockConversationListMenuElement] =
    deriveDecoder[SlackBlockConversationListMenuElement]

  implicit val encoderSlackBlockChannelsListMenuElement: Encoder.AsObject[SlackBlockChannelsListMenuElement] =
    deriveEncoder[SlackBlockChannelsListMenuElement]

  implicit val decoderSlackBlockChannelsListMenuElement: Decoder[SlackBlockChannelsListMenuElement] =
    deriveDecoder[SlackBlockChannelsListMenuElement]

  implicit val encoderSlackBlockRichTextQuote: Encoder.AsObject[SlackBlockRichTextQuote] =
    deriveEncoder[SlackBlockRichTextQuote]
  implicit val decoderSlackBlockRichTextQuote: Decoder[SlackBlockRichTextQuote] = deriveDecoder[SlackBlockRichTextQuote]

  implicit val encoderSlackBlockExternalMenuElement: Encoder.AsObject[SlackBlockExternalMenuElement] =
    deriveEncoder[SlackBlockExternalMenuElement]

  implicit val decoderSlackBlockExternalMenuElement: Decoder[SlackBlockExternalMenuElement] =
    deriveDecoder[SlackBlockExternalMenuElement]

  implicit val encoderSlackBlockOverflowMenuElement: Encoder.AsObject[SlackBlockOverflowMenuElement] =
    deriveEncoder[SlackBlockOverflowMenuElement]

  implicit val decoderSlackBlockOverflowMenuElement: Decoder[SlackBlockOverflowMenuElement] =
    deriveDecoder[SlackBlockOverflowMenuElement]

  implicit val encoderSlackBlockPlainInputElement: Encoder.AsObject[SlackBlockPlainInputElement] =
    deriveEncoder[SlackBlockPlainInputElement]

  implicit val decoderSlackBlockPlainInputElement: Decoder[SlackBlockPlainInputElement] =
    deriveDecoder[SlackBlockPlainInputElement]

  implicit val encoderSlackBlockDatePickerElement: Encoder.AsObject[SlackBlockDatePickerElement] =
    deriveEncoder[SlackBlockDatePickerElement]

  implicit val decoderSlackBlockDatePickerElement: Decoder[SlackBlockDatePickerElement] =
    deriveDecoder[SlackBlockDatePickerElement]

  implicit val encoderSlackBlockUsersListMenuElement: Encoder.AsObject[SlackBlockUsersListMenuElement] =
    deriveEncoder[SlackBlockUsersListMenuElement]

  implicit val decoderSlackBlockUsersListMenuElement: Decoder[SlackBlockUsersListMenuElement] =
    deriveDecoder[SlackBlockUsersListMenuElement]

  implicit val encoder: Encoder[SlackBlockElement] = JsonTaggedAdtCodec.createEncoder[SlackBlockElement]( "type" )
  implicit val decoder: Decoder[SlackBlockElement] = JsonTaggedAdtCodec.createDecoder[SlackBlockElement]( "type" )
}

@JsonAdt( "image" )
case class SlackBlockImageElement( image_url: String, alt_text: Option[String] = None ) extends SlackBlockElement

@JsonAdt( "button" )
case class SlackBlockButtonElement(
    text: SlackBlockText,
    action_id: String,
    url: Option[String] = None,
    value: Option[String] = None,
    style: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockElement

case class SlackBlockConfirmItem(
    title: SlackBlockPlainText,
    text: SlackBlockText,
    confirm: SlackBlockPlainText,
    deny: SlackBlockPlainText
)

sealed trait SlackBlockMenuElement extends SlackBlockElement

@JsonAdt( "static_select" )
case class SlackBlockStaticMenuElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    options: Option[NonEmptyList[SlackBlockOptionItem]] = None,
    option_groups: Option[NonEmptyList[SlackBlockOptionGroup]] = None,
    initial_option: Option[SlackBlockOptionItem] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockMenuElement

@JsonAdt( "external_select" )
case class SlackBlockExternalMenuElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_option: Option[SlackBlockOptionItem] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockMenuElement

@JsonAdt( "users_select" )
case class SlackBlockUsersListMenuElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_user: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockMenuElement

@JsonAdt( "conversations_select" )
case class SlackBlockConversationListMenuElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_conversation: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockMenuElement

@JsonAdt( "channels_select" )
case class SlackBlockChannelsListMenuElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_channel: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockMenuElement

@JsonAdt( "overflow" )
case class SlackBlockOverflowMenuElement(
    action_id: String,
    options: NonEmptyList[SlackBlockOptionItem],
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockMenuElement

case class SlackBlockOptionItem( text: SlackBlockPlainText, value: String )

case class SlackBlockOptionGroup( label: SlackBlockPlainText, options: List[SlackBlockOptionItem] )

@JsonAdt( "datepicker" )
case class SlackBlockDatePickerElement(
    action_id: String,
    placeholder: Option[SlackBlockPlainText] = None,
    initial_date: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockElement

@JsonAdt( "plain_text_input" )
case class SlackBlockPlainInputElement(
    action_id: String,
    placeholder: Option[SlackBlockPlainText] = None,
    initial_value: Option[String] = None,
    multiline: Option[Boolean] = None,
    min_length: Option[Int] = None,
    max_length: Option[Long] = None
) extends SlackBlockElement

@JsonAdt( "rich_text_section" )
case class SlackBlockRichTextSection() extends SlackBlockElement

@JsonAdt( "rich_text_preformatted" )
case class SlackBlockRichTextPreformatted() extends SlackBlockElement

@JsonAdt( "rich_text_list" )
case class SlackBlockRichTextList() extends SlackBlockElement

@JsonAdt( "rich_text_quote" )
case class SlackBlockRichTextQuote() extends SlackBlockElement
