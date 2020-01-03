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

trait SlackBlocksTemplateDsl {

  implicit final class SlackTextInterpolators( private val sc: StringContext ) {

    private def applySubs( subs: Seq[Any] ): String = {
      if (subs.nonEmpty)
        sc.s( subs: _* )
      else
        sc.s()
    }

    def plain( subs: Any* ): SlackBlockPlainText = {
      SlackBlockPlainText(
        text = applySubs( subs )
      )
    }

    def md( subs: Any* ): SlackBlockMarkDownText = {
      SlackBlockMarkDownText(
        text = applySubs( subs )
      )
    }

  }

  private def noneIfEmptyList[T]( xs: => List[Option[T]] ): Option[List[T]] = xs.flatten match {
    case Nil => None
    case xs  => Some( xs )
  }

  private def optElement[T]( condition: => Boolean = true, element: => T ) =
    if (condition)
      Option( element )
    else
      None

  protected def blocks( blockDefs: Option[SlackBlock]* ): Option[List[SlackBlock]] =
    noneIfEmptyList( blockDefs.toList )

  protected def blocksGroup( blockDefs: Option[List[SlackBlock]]* ): Option[List[SlackBlock]] =
    blockDefs.toList.flatten.flatten match {
      case Nil => None
      case xs  => Some( xs )
    }

  protected def dividerBlock = SlackDividerBlock
  protected def sectionBlock = SlackSectionBlock
  protected def inputBlock = SlackInputBlock
  protected def contextBlock = SlackContextBlock
  protected def fileBlock = SlackFileBlock
  protected def actionsBlock = SlackActionsBlock
  protected def imageBlock = SlackImageBlock
  protected def richBlock = SlackRichTextBlock

  protected def block[BT <: SlackBlock]( block: => BT ): Option[BT] = Option( block )

  protected def optBlock[BT <: SlackBlock]( condition: => Boolean = true )( block: => BT ): Option[BT] =
    optElement( condition, block )

  protected def sectionField[BT <: SlackBlockText]( field: => BT ): Option[BT] = Option( field )

  protected def optSectionField[BT <: SlackBlockText]( condition: => Boolean = true )( field: => BT ): Option[BT] =
    optElement( condition, field )

  protected def sectionFields( sectionFieldDefs: Option[SlackBlockText]* ): Option[List[SlackBlockText]] =
    noneIfEmptyList( sectionFieldDefs.toList )

  protected def blockElements(
      defs: Option[SlackBlockElement]*
  ): NonEmptyList[SlackBlockElement] =
    NonEmptyList.fromListUnsafe( defs.flatten.toList )

  protected def blockEl[BE <: SlackBlockElement]( blockElement: => BE ): Option[BE] = Option( blockElement )

  protected def optBlockEl[BE <: SlackBlockElement]( condition: => Boolean = true )( blockElement: => BE ): Option[BE] =
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

  protected def menuItems( defs: Option[SlackBlockOptionItem]* ): NonEmptyList[SlackBlockOptionItem] =
    NonEmptyList.fromListUnsafe( defs.flatten.toList )

  protected def staticMenuItems( defs: Option[SlackBlockOptionItem]* ): Option[NonEmptyList[SlackBlockOptionItem]] =
    Some( menuItems( defs: _* ) )

  protected def menuItem( item: => SlackBlockOptionItem ): Option[SlackBlockOptionItem] = Option( item )

  protected def optMenuItem(
      condition: => Boolean = true
  )( item: => SlackBlockOptionItem ): Option[SlackBlockOptionItem] =
    optElement( condition, item )

  protected def menuItemValue = SlackBlockOptionItem

  protected def menuGroups( defs: Option[SlackBlockOptionGroup]* ): NonEmptyList[SlackBlockOptionGroup] =
    NonEmptyList.fromListUnsafe( defs.flatten.toList )

  protected def staticMenuGroups( defs: Option[SlackBlockOptionGroup]* ): Option[NonEmptyList[SlackBlockOptionGroup]] =
    Option( menuGroups( defs: _* ) )

  protected def menuGroup( group: => SlackBlockOptionGroup ): Option[SlackBlockOptionGroup] = Option( group )

  protected def optMenuGroup(
      condition: => Boolean = true
  )( group: => SlackBlockOptionGroup ): Option[SlackBlockOptionGroup] =
    optElement( condition, group )

  protected def menuGroupValue = SlackBlockOptionGroup

  protected def confirm( value: => SlackBlockConfirmItem ): Option[SlackBlockConfirmItem] = Option( value )
  protected def confirmValue = SlackBlockConfirmItem

}
