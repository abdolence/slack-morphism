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

import scala.language.implicitConversions

trait SlackBlocksTemplateDsl extends SlackBlocksTemplateDslInternals {

  protected def blocks( blockDefs: SlackDslItemDef[SlackBlock]* ): List[SlackBlock] =
    blockDefs.toList.flatten

  protected def blocksOrEmpty( blockDefs: SlackDslItemDef[SlackBlock]* ): Option[List[SlackBlock]] =
    noneIfEmptyList( blocks( blockDefs: _* ) )

  protected def optBlocks( condition: => Boolean )( blockDefs: SlackDslItemDef[SlackBlock]* ): List[SlackBlock] =
    if (condition) {
      blocks( blockDefs: _* )
    } else List.empty

  protected def optBlock[T <: SlackBlock]( condition: => Boolean )( block: => T ): SlackDslItemDef[T] =
    optElement( condition, block )

  protected def dividerBlock = SlackDividerBlock
  protected def sectionBlock = SlackSectionBlock
  protected def inputBlock = SlackInputBlock
  protected def contextBlock = SlackContextBlock
  protected def fileBlock = SlackFileBlock
  protected def actionsBlock = SlackActionsBlock
  protected def imageBlock = SlackImageBlock
  protected def richBlock = SlackRichTextBlock

  protected def sectionFields( defs: SlackDslItemDef[SlackBlockText]* ): Option[List[SlackBlockText]] =
    noneIfEmptyList( defs.toList.flatten )

  protected def optSectionField[T <: SlackBlockText]( condition: => Boolean )( field: => T ): SlackDslItemDef[T] =
    optElement( condition, field )

  protected def blockElements[T <: SlackBlockElement](
      defs: SlackDslItemDef[T]*
  ): NonEmptyList[T] = NonEmptyList.fromListUnsafe( defs.toList.flatten )

  protected def optBlockEl[T <: SlackBlockElement]( condition: => Boolean )( blockElement: => T ): SlackDslItemDef[T] =
    optElement( condition, blockElement )

  protected def button = SlackBlockButtonElement
  protected def image = SlackBlockImageElement
  protected def datePicker = SlackBlockDatePickerElement

  protected def overflow = SlackBlockOverflowElement

  protected def usersSelect = SlackBlockUsersListSelectElement
  protected def conversationsSelect = SlackBlockConversationListSelectElement
  protected def channelsSelect = SlackBlockChannelsListSelectElement
  protected def staticSelect = SlackBlockStaticSelectElement
  protected def externalSelect = SlackBlockExternalSelectElement

  protected def multiUsersSelect = SlackBlockMultiUsersListSelectElement
  protected def multiConversationsSelect = SlackBlockMultiConversationListSelectElement
  protected def multiChannelsSelect = SlackBlockMultiChannelsListSelectElement
  protected def multiStaticSelect = SlackBlockMultiStaticSelectElement
  protected def multiExternalSelect = SlackBlockMultiExternalSelectElement

  protected def radioButtons = SlackBlockRadioButtonsElement

  protected def choiceItems( defs: SlackDslItemDef[SlackBlockChoiceItem]* ): NonEmptyList[SlackBlockChoiceItem] =
    NonEmptyList.fromListUnsafe( defs.toList.flatten )

  protected def choiceStrItems( defs: String* ): NonEmptyList[String] =
    NonEmptyList.fromListUnsafe( defs.toList )

  protected def optChoiceItem(
      condition: => Boolean
  )( item: => SlackBlockChoiceItem ): SlackDslItemDef[SlackBlockChoiceItem] =
    optElement( condition, item )

  protected def choiceItem = SlackBlockChoiceItem

  protected def choiceGroups( defs: SlackDslItemDef[SlackBlockOptionGroup]* ): NonEmptyList[SlackBlockOptionGroup] =
    NonEmptyList.fromListUnsafe( defs.flatten.toList )

  protected def choiceGroup = SlackBlockOptionGroup

  protected def optChoiceGroup(
      condition: => Boolean
  )( group: => SlackBlockOptionGroup ): SlackDslItemDef[SlackBlockOptionGroup] =
    optElement( condition, group )

  protected def confirm = SlackBlockConfirmItem

}
