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

package org.latestbit.slack.morphism.client.templating

import org.latestbit.slack.morphism.messages._
import cats.data._

trait SlackBlocksTemplateDsl extends SlackBlocksTemplateDslInternals {

  protected def blocks( blockDefs: SlackDslItemDef[SlackBlock]* ): List[SlackBlock] =
    blockDefs.toList.flatten

  protected def blocksOrEmpty( blockDefs: SlackDslItemDef[SlackBlock]* ): Option[List[SlackBlock]] =
    noneIfEmptyList( blocks( blockDefs: _* ) )

  protected def optBlocks( condition: => Boolean )( blockDefs: SlackDslItemDef[SlackBlock]* ): List[SlackBlock] =
    if (condition) {
      blocks( blockDefs: _* )
    } else List.empty

  protected def dividerBlock = SlackDividerBlock
  protected def sectionBlock = SlackSectionBlock
  protected def inputBlock   = SlackInputBlock
  protected def contextBlock = SlackContextBlock
  protected def fileBlock    = SlackFileBlock
  protected def actionsBlock = SlackActionsBlock
  protected def imageBlock   = SlackImageBlock
  protected def headerBlock  = SlackHeaderBlock
  protected def richBlock    = SlackRichTextBlock

  protected def sectionFields( defs: SlackDslItemDef[SlackBlockText]* ): Option[List[SlackBlockText]] =
    noneIfEmptyList( defs.toList.flatten )

  protected def blockElements[T <: SlackBlockElement](
      defs: SlackDslItemDef[T]*
  ): NonEmptyList[T] = NonEmptyList.fromListUnsafe( defs.toList.flatten )

  protected def button     = SlackBlockButtonElement
  protected def image      = SlackBlockImageElement
  protected def datePicker = SlackBlockDatePickerElement

  protected def overflow = SlackBlockOverflowElement

  protected def usersSelect         = SlackBlockUsersListSelectElement
  protected def conversationsSelect = SlackBlockConversationListSelectElement
  protected def channelsSelect      = SlackBlockChannelsListSelectElement
  protected def staticSelect        = SlackBlockStaticSelectElement
  protected def externalSelect      = SlackBlockExternalSelectElement

  protected def multiUsersSelect         = SlackBlockMultiUsersListSelectElement
  protected def multiConversationsSelect = SlackBlockMultiConversationListSelectElement
  protected def multiChannelsSelect      = SlackBlockMultiChannelsListSelectElement
  protected def multiStaticSelect        = SlackBlockMultiStaticSelectElement
  protected def multiExternalSelect      = SlackBlockMultiExternalSelectElement

  protected def radioButtons = SlackBlockRadioButtonsElement

  protected def checkboxes = SlackBlockCheckboxesElement

  protected def choiceItems[T <: SlackBlockText](
      defs: SlackDslItemDef[SlackBlockChoiceItem[T]]*
  ): NonEmptyList[SlackBlockChoiceItem[T]] =
    NonEmptyList.fromListUnsafe( defs.toList.flatten )

  protected def choiceStrItems( defs: String* ): NonEmptyList[String] =
    NonEmptyList.fromListUnsafe( defs.toList )

  protected def choiceItem = SlackBlockChoiceItem

  protected def choiceGroups[T <: SlackBlockText](
      defs: SlackDslItemDef[SlackBlockOptionGroup[T]]*
  ): NonEmptyList[SlackBlockOptionGroup[T]] =
    NonEmptyList.fromListUnsafe( defs.flatten.toList )

  protected def choiceGroup = SlackBlockOptionGroup

  protected def confirm = SlackBlockConfirmItem

  protected def optionally[T]( condition: => Boolean )( createItem: => T ): SlackDslItemDef[T] = {
    optElement[T]( condition, createItem )
  }

}
