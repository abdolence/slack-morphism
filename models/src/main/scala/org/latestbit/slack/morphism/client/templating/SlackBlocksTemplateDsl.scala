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

  protected def blockElements(
      defs: SlackDslItemDef[SlackBlockElement]*
  ): NonEmptyList[SlackBlockElement] = NonEmptyList.fromListUnsafe( defs.toList.flatten )

  protected def optBlockEl[T <: SlackBlockElement]( condition: => Boolean )( blockElement: => T ): SlackDslItemDef[T] =
    optElement( condition, blockElement )

  protected def button = SlackBlockButtonElement
  protected def image = SlackBlockImageElement
  protected def datePicker = SlackBlockDatePickerElement

  protected def overflowMenu = SlackBlockOverflowMenuElement

  protected def usersListMenu = SlackBlockUsersListMenuElement
  protected def conversationsListMenu = SlackBlockConversationListMenuElement
  protected def channelsListMenu = SlackBlockChannelsListMenuElement
  protected def staticMenu = SlackBlockStaticMenuElement
  protected def externalMenu = SlackBlockExternalMenuElement

  protected def multiUsersListMenu = SlackBlockMultiUsersListMenuElement
  protected def multiConversationsListMenu = SlackBlockMultiConversationListMenuElement
  protected def multiChannelsListMenu = SlackBlockMultiChannelsListMenuElement
  protected def multiStaticMenu = SlackBlockMultiStaticMenuElement
  protected def multiExternalMenu = SlackBlockMultiExternalMenuElement

  protected def radioButtons = SlackBlockRadioButtonsElement

  protected def choiceItems( defs: SlackDslItemDef[SlackBlockOptionItem]* ): NonEmptyList[SlackBlockOptionItem] =
    NonEmptyList.fromListUnsafe( defs.toList.flatten )

  protected def staticChoiceItems(
      defs: SlackDslItemDef[SlackBlockOptionItem]*
  ): Option[NonEmptyList[SlackBlockOptionItem]] =
    Some( choiceItems( defs: _* ) )

  protected def optChoiceItem(
      condition: => Boolean
  )( item: => SlackBlockOptionItem ): SlackDslItemDef[SlackBlockOptionItem] =
    optElement( condition, item )

  protected def choiceItem = SlackBlockOptionItem

  protected def choiceGroups( defs: SlackDslItemDef[SlackBlockOptionGroup]* ): NonEmptyList[SlackBlockOptionGroup] =
    NonEmptyList.fromListUnsafe( defs.flatten.toList )

  protected def staticChoiceGroups(
      defs: SlackDslItemDef[SlackBlockOptionGroup]*
  ): Option[NonEmptyList[SlackBlockOptionGroup]] =
    Option( choiceGroups( defs: _* ) )

  protected def choiceGroup = SlackBlockOptionGroup

  protected def optChoiceGroup(
      condition: => Boolean
  )( group: => SlackBlockOptionGroup ): SlackDslItemDef[SlackBlockOptionGroup] =
    optElement( condition, group )

  protected def confirm = SlackBlockConfirmItem

}
