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

import org.latestbit.slack.morphism.messages.{
  SlackBlock,
  SlackBlockConfirmItem,
  SlackBlockElement,
  SlackBlockMarkDownText,
  SlackBlockOptionItem,
  SlackBlockPlainText,
  SlackBlockText
}

import scala.language.implicitConversions

trait SlackBlocksTemplateDslInternals {

  sealed trait SlackDslItemDef[+T]
  case class SlackDslSomeItem[+T]( item: () => T ) extends SlackDslItemDef[T]
  case class SlackDslSomeIterableOfItem[+T]( item: () => Iterable[T] ) extends SlackDslItemDef[T]
  case object SlackDslNoneItem extends SlackDslItemDef[Nothing]

  implicit final def slackBlockToDef( block: => SlackBlock ) =
    SlackDslSomeItem[SlackBlock](() => block )

  implicit final def slackBlockIterableToDef( block: => Iterable[SlackBlock] ) =
    SlackDslSomeIterableOfItem[SlackBlock](() => block )

  implicit final def slackBlockElToDef( blockEl: => SlackBlockElement ) =
    SlackDslSomeItem[SlackBlockElement](() => blockEl )

  implicit final def slackBlockTextToDef( blockEl: => SlackBlockText ) =
    SlackDslSomeItem[SlackBlockText](() => blockEl )

  implicit final def slackBlockOptionItemToDef( item: => SlackBlockOptionItem ) =
    SlackDslSomeItem[SlackBlockOptionItem](() => item )

  implicit def slackBlockElementToOption( el: SlackBlockElement ): Option[SlackBlockElement] = Some( el )
  implicit def slackBlockConfirmItemToOption( el: SlackBlockConfirmItem ): Option[SlackBlockConfirmItem] = Some( el )

  implicit def slackBlocksListToOption( blocks: List[SlackBlock] ): Option[List[SlackBlock]] = noneIfEmptyList( blocks )

  implicit def slackDslItemDefToIterable[T]( itemDef: SlackDslItemDef[T] ): Iterable[T] = itemDef match {
    case SlackDslSomeItem( item )            => List( item() ) // We can't use Iterable.single( item() ) in Scala 2.12
    case SlackDslSomeIterableOfItem( items ) => items()
    case SlackDslNoneItem                    => Iterable.empty
  }

  implicit final def slackDslListInnerItemsToListItems[T]( items: => Iterable[Iterable[T]] ) =
    SlackDslSomeIterableOfItem[T](() => items.flatten )

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

  private[templating] def noneIfEmptyList[T]( xs: => List[T] ): Option[List[T]] = xs match {
    case Nil => None
    case xs  => Some( xs )
  }

  private[templating] def optElement[T]( condition: => Boolean, element: => T ): SlackDslItemDef[T] =
    if (condition)
      SlackDslSomeItem[T](() => element )
    else
      SlackDslNoneItem
}
