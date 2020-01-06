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

package org.latestbit.slack.morphism.concurrent

import scala.concurrent.{ ExecutionContext, Future }

trait AsyncSeqIteratorPosition[A, +P] {
  def getPos( item: A ): Option[P]
}

/**
 * Async iterator is able to provide infinite async computed values lazily iterating over some function
 * Unlike standard Stream[]/LazyList[] from Scala, this implementation doesn't memorise previous values.
 * Unlike Future.sequence/fold we don't know beforehand how many async actions are coming
 *
 * Look at [[AsyncSeqIterator.cons]] for details.
 *
 * @note It is not possible to implement standard Iterator[] because of the sync nature of hasNext.
 *
 * @tparam I iterating over item type which has a some position
 * @tparam A extracted value type (extracted from I)
 */
trait AsyncSeqIterator[+I, +A] {

  def item()(
      implicit ec: ExecutionContext
  ): Future[I]

  def value()(
      implicit ec: ExecutionContext
  ): Future[A]

  def next()( implicit ec: ExecutionContext ): Future[Option[AsyncSeqIterator[I, A]]]

  def foldLeft[B](
      initial: B
  )( f: ( B, A ) => B )( implicit ec: ExecutionContext ): Future[B] = {
    value().flatMap { currentValue =>
      val folded = f( initial, currentValue )
      next().flatMap {
        case Some( nextIterator ) => {
          nextIterator.foldLeft( folded )( f )
        }
        case _ => {
          Future.successful( folded )
        }
      }
    }
  }

  def map[B](
      f: A => B
  ): AsyncSeqIterator[I, B]

}

/**
 * Async iterator constructors
 */
object AsyncSeqIterator {

  /**
   * Async iterator constructor
   */
  object cons {

    /**
     * Constructor of an async iterator over some sequence of futures,
     * generated by a provided function and previous state in a sequence
     *
     * Suppose we have Future[MyItem] and we would like to infinitely iterate using computed next item
     * and previous state without storing all the previous data:
     *
     * {{{
     * case class MyItem(value : String, cursor : Option[Int])
     *
     * def initialItem() : Future[MyItem] = ??? // Any implementation
     * def nextItem(cursor : String) : Future[MyItem] = ??? // Any implementation
     *
     * val iterator = AsyncSeqIterator.cons[MyItem, String, Int] (
     *  initial = initialItem(),
     *  toValue = _.value,
     *  getPos = _.cursor,
     *  producer = nextItem
     * ) // resulting in AsyncSeqIterator[MyItem, String]
     *
     * val computedList : Future[List[String]] = iterator.foldLeft( List[String]() ) { case (all, itemValue) =>
     *   all :+ itemValue
     * }
     *
     * }}}
     *
     * @param initial initial async item
     * @param toValue function to extract value from a provided item
     * @param getPos function to extract the current state or position of current item. When a returned position is None than the whole sequence is completely finished
     * @param producer generator of next item based on a state/position in previous item
     * @tparam I item type
     * @tparam A value type
     * @tparam P state/position type
     * @return an async iterator instance
     */
    def apply[I, A, P](
        initial: => Future[I],
        toValue: I => A,
        getPos: I => Option[P],
        producer: P => Future[I]
    ): AsyncSeqIterator[I, A] = {
      new AsyncSeqIterator[I, A] {
        private lazy val computed: Future[I] = initial

        override def item()(
            implicit ec: ExecutionContext
        ): Future[I] = computed

        override def value()(
            implicit ec: ExecutionContext
        ): Future[A] = computed.map( toValue )

        override def next()(
            implicit ec: ExecutionContext
        ): Future[Option[AsyncSeqIterator[I, A]]] = {
          computed.map { computedValue =>
            getPos( computedValue ).map { pos =>
              cons(
                producer( pos ),
                toValue,
                getPos,
                producer
              )
            }
          }
        }

        override def map[B]( f: A => B ): AsyncSeqIterator[I, B] = {
          cons(
            initial,
            toValue.andThen( f ),
            getPos,
            producer
          )
        }

      }
    }
  }

}
