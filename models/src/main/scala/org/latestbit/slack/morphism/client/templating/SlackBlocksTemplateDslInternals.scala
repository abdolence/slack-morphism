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

import cats.data.NonEmptyList
import org.latestbit.slack.morphism.messages.{
  SlackBlock,
  SlackBlockConfirmItem,
  SlackBlockElement,
  SlackBlockMarkDownText,
  SlackBlockChoiceItem,
  SlackBlockPlainText,
  SlackBlockText
}

import scala.language.implicitConversions

trait SlackBlocksTemplateDslInternals {

  protected sealed trait SlackDslItemDef[+T]
  protected case class SlackDslSomeItem[+T]( item: () => T )                     extends SlackDslItemDef[T]
  protected case class SlackDslSomeIterableOfItem[+T]( item: () => Iterable[T] ) extends SlackDslItemDef[T]
  protected case object SlackDslNoneItem                                         extends SlackDslItemDef[Nothing]

  protected implicit final def slackBlockToDef( block: => SlackBlock ) =
    SlackDslSomeItem[SlackBlock]( () => block )

  protected implicit final def slackBlockIterableToDef( block: => Iterable[SlackBlock] ) =
    SlackDslSomeIterableOfItem[SlackBlock]( () => block )

  protected implicit final def slackBlockElToDef[T <: SlackBlockElement]( blockEl: => T ) =
    SlackDslSomeItem[T]( () => blockEl )

  protected implicit final def slackBlockTextToDef[T <: SlackBlockText]( blockEl: => T ) =
    SlackDslSomeItem[T]( () => blockEl )

  protected implicit final def slackBlockOptionItemToDef[T <: SlackBlockText]( item: => SlackBlockChoiceItem[T] ) =
    SlackDslSomeItem[SlackBlockChoiceItem[T]]( () => item )

  protected implicit def slackBlockElementToOption[T <: SlackBlockElement]( el: T ): Option[T] = Some( el )

  protected implicit def slackBlockConfirmItemToOption( el: SlackBlockConfirmItem ): Option[SlackBlockConfirmItem] =
    Some( el )

  protected implicit def slackBlockNonEmptyListToOption[T]( els: NonEmptyList[T] ): Option[NonEmptyList[T]] =
    Some( els )

  protected implicit def slackBlocksListToOption( blocks: List[SlackBlock] ): Option[List[SlackBlock]] =
    noneIfEmptyList( blocks )

  protected implicit def slackDslItemDefToIterable[T]( itemDef: SlackDslItemDef[T] ): Iterable[T] =
    itemDef match {
      case SlackDslSomeItem( item )            => List( item() ) // We can't use Iterable.single( item() ) in Scala 2.12
      case SlackDslSomeIterableOfItem( items ) => items()
      case SlackDslNoneItem                    => Iterable.empty
    }

  protected implicit final def slackDslListInnerItemsToListItems[T]( items: => Iterable[Iterable[T]] ) =
    SlackDslSomeIterableOfItem[T]( () => items.flatten )

  protected implicit final class SlackTextInterpolators( private val sc: StringContext ) {

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

    def markdown( subs: Any* ): SlackBlockMarkDownText = {
      SlackBlockMarkDownText(
        text = applySubs( subs )
      )
    }

    def md( subs: Any* ) = markdown( subs: _* )
    def pt( subs: Any* ) = plain( subs: _* )
  }

  private[templating] def noneIfEmptyList[T]( xs: => List[T] ): Option[List[T]] =
    xs match {
      case Nil => None
      case xs  => Some( xs )
    }

  private[templating] def optElement[T]( condition: => Boolean, element: => T ): SlackDslItemDef[T] =
    if (condition)
      SlackDslSomeItem[T]( () => element )
    else
      SlackDslNoneItem
}
