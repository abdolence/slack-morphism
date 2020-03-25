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
import org.latestbit.circe.adt.codec._

/**
 * Block elements can be used inside of section, context, and actions layout blocks. Inputs can only be used inside of input blocks.
 * https://api.slack.com/reference/block-kit/block-elements
 */
sealed trait SlackBlockElement

/**
 * Block elements allowed in Slack Section Block
 */
@JsonAdtPassThrough
sealed trait SlackSectionBlockElement extends SlackBlockElement

/**
 * Block elements allowed in Slack Context Block
 */
@JsonAdtPassThrough
sealed trait SlackContextBlockElement extends SlackBlockElement

/**
 * Block elements allowed in Slack Action Block
 */
@JsonAdtPassThrough
sealed trait SlackActionBlockElement extends SlackBlockElement

/**
 * Block elements allowed in Slack Input Block
 */
@JsonAdtPassThrough
sealed trait SlackInputBlockElement extends SlackBlockElement

/**
 * Block elements allowed in Slack Rich Block
 */
@JsonAdtPassThrough
sealed trait SlackRichBlockElement extends SlackBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#image
 */
@JsonAdt( "image" )
case class SlackBlockImageElement( image_url: String, alt_text: String )
    extends SlackBlockElement
    with SlackSectionBlockElement
    with SlackContextBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#button
 */
@JsonAdt( "button" )
case class SlackBlockButtonElement(
    text: SlackBlockPlainText,
    action_id: String,
    url: Option[String] = None,
    value: Option[String] = None,
    style: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockElement
    with SlackSectionBlockElement
    with SlackActionBlockElement

/**
 * https://api.slack.com/reference/block-kit/composition-objects#confirm
 */
case class SlackBlockConfirmItem(
    title: SlackBlockPlainText,
    text: SlackBlockText,
    confirm: SlackBlockPlainText,
    deny: SlackBlockPlainText
)

/**
 * https://api.slack.com/reference/block-kit/block-elements
 */
@JsonAdtPassThrough
sealed trait SlackBlockSelectElement extends SlackBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#select
 */
@JsonAdt( "static_select" )
case class SlackBlockStaticSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    options: Option[NonEmptyList[SlackBlockChoiceItem[SlackBlockPlainText]]] = None,
    option_groups: Option[NonEmptyList[SlackBlockOptionGroup[SlackBlockPlainText]]] = None,
    initial_option: Option[SlackBlockChoiceItem[SlackBlockPlainText]] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement {
  require( options.nonEmpty || option_groups.nonEmpty, "Either `options` or `option_groups` should be defined" )
}

/**
 * https://api.slack.com/reference/block-kit/block-elements#multi_select
 */
@JsonAdt( "multi_static_select" )
case class SlackBlockMultiStaticSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    options: Option[NonEmptyList[SlackBlockChoiceItem[SlackBlockPlainText]]] = None,
    option_groups: Option[NonEmptyList[SlackBlockOptionGroup[SlackBlockPlainText]]] = None,
    initial_options: Option[NonEmptyList[SlackBlockChoiceItem[SlackBlockPlainText]]] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    max_selected_items: Option[Long] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement {
  require( options.nonEmpty || option_groups.nonEmpty, "Either `options` or `option_groups` should be defined" )
}

/**
 * https://api.slack.com/reference/block-kit/block-elements#external_select
 */
@JsonAdt( "external_select" )
case class SlackBlockExternalSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_option: Option[SlackBlockChoiceItem[SlackBlockPlainText]] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#multi_external_select
 */
@JsonAdt( "multi_external_select" )
case class SlackBlockMultiExternalSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_options: Option[NonEmptyList[SlackBlockChoiceItem[SlackBlockPlainText]]] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    max_selected_items: Option[Long] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#users_select
 */
@JsonAdt( "users_select" )
case class SlackBlockUsersListSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_user: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#multi_users_select
 */
@JsonAdt( "multi_users_select" )
case class SlackBlockMultiUsersListSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_users: Option[NonEmptyList[String]] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    max_selected_items: Option[Long] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#conversations_select
 */
@JsonAdt( "conversations_select" )
case class SlackBlockConversationListSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_conversation: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    response_url_enabled: Option[Boolean] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#multi_conversations_select
 */
@JsonAdt( "multi_conversations_select" )
case class SlackBlockMultiConversationListSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_conversations: Option[NonEmptyList[String]] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    max_selected_items: Option[Long] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#channels_select
 */
@JsonAdt( "channels_select" )
case class SlackBlockChannelsListSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_channel: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    response_url_enabled: Option[Boolean] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#multi_channels_select
 */
@JsonAdt( "multi_channels_select" )
case class SlackBlockMultiChannelsListSelectElement(
    placeholder: SlackBlockPlainText,
    action_id: String,
    initial_channels: Option[NonEmptyList[String]] = None,
    confirm: Option[SlackBlockConfirmItem] = None,
    max_selected_items: Option[Long] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#overflow
 */
@JsonAdt( "overflow" )
case class SlackBlockOverflowElement(
    action_id: String,
    options: NonEmptyList[SlackBlockChoiceItem[SlackBlockPlainText]],
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockSelectElement
    with SlackSectionBlockElement
    with SlackActionBlockElement

case class SlackBlockChoiceItem[+T <: SlackBlockText]( text: T, value: String, url: Option[String] = None )

case class SlackBlockOptionGroup[+T <: SlackBlockText](
    label: SlackBlockPlainText,
    options: List[SlackBlockChoiceItem[T]]
)

/**
 * https://api.slack.com/reference/block-kit/block-elements#datepicker
 */
@JsonAdt( "datepicker" )
case class SlackBlockDatePickerElement(
    action_id: String,
    placeholder: Option[SlackBlockPlainText] = None,
    initial_date: Option[String] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockElement
    with SlackSectionBlockElement
    with SlackActionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#input
 */
@JsonAdt( "plain_text_input" )
case class SlackBlockPlainInputElement(
    action_id: String,
    placeholder: Option[SlackBlockPlainText] = None,
    initial_value: Option[String] = None,
    multiline: Option[Boolean] = None,
    min_length: Option[Int] = None,
    max_length: Option[Long] = None
) extends SlackBlockElement
    with SlackSectionBlockElement
    with SlackActionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#radio
 */
@JsonAdt( "radio_buttons" )
case class SlackBlockRadioButtonsElement(
    action_id: String,
    options: NonEmptyList[SlackBlockChoiceItem[SlackBlockText]],
    initial_options: Option[NonEmptyList[SlackBlockChoiceItem[SlackBlockText]]] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockElement
    with SlackSectionBlockElement
    with SlackActionBlockElement
    with SlackInputBlockElement

/**
 * https://api.slack.com/reference/block-kit/block-elements#checkboxes
 */
@JsonAdt( "checkboxes" )
case class SlackBlockCheckboxesElement(
    action_id: String,
    options: NonEmptyList[SlackBlockChoiceItem[SlackBlockText]],
    initial_options: Option[NonEmptyList[SlackBlockChoiceItem[SlackBlockText]]] = None,
    confirm: Option[SlackBlockConfirmItem] = None
) extends SlackBlockElement
    with SlackSectionBlockElement
    with SlackActionBlockElement
    with SlackInputBlockElement

@JsonAdt( "rich_text_section" )
case class SlackBlockRichTextSection() extends SlackBlockElement with SlackRichBlockElement

@JsonAdt( "rich_text_preformatted" )
case class SlackBlockRichTextPreformatted() extends SlackBlockElement with SlackRichBlockElement

@JsonAdt( "rich_text_list" )
case class SlackBlockRichTextList() extends SlackBlockElement with SlackRichBlockElement

@JsonAdt( "rich_text_quote" )
case class SlackBlockRichTextQuote() extends SlackBlockElement with SlackRichBlockElement

object SlackBlockTextTypes {
  final val MARK_DOWN = "mrkdwn"
  final val PLAIN_TEXT = "plain_text"
}

/**
 * https://api.slack.com/reference/block-kit/composition-objects#text
 */
@JsonAdtPassThrough
sealed trait SlackBlockText extends SlackBlockElement with SlackContextBlockElement

/**
 * 'plain_text' type of https://api.slack.com/reference/block-kit/composition-objects#text
 */
@JsonAdt( SlackBlockTextTypes.PLAIN_TEXT )
case class SlackBlockPlainText(
    text: String,
    emoji: Option[Boolean] = None
) extends SlackBlockText

/**
 * 'mrkdwn' type of https://api.slack.com/reference/block-kit/composition-objects#text
 */
@JsonAdt( SlackBlockTextTypes.MARK_DOWN )
case class SlackBlockMarkDownText(
    text: String,
    verbatim: Option[Boolean] = None
) extends SlackBlockText
