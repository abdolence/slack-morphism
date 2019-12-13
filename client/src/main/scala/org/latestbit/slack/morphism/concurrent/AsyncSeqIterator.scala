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

trait AsyncSeqIterator[+A, +V] {

  def item()(
      implicit ec: ExecutionContext
  ): Future[A]

  def value()(
      implicit ec: ExecutionContext
  ): Future[V]

  def next()( implicit ec: ExecutionContext ): Future[Option[AsyncSeqIterator[A, V]]]

  def foldLeft[B](
      initial: B
  )( f: ( B, V ) => B )( implicit ec: ExecutionContext ): Future[B] = {
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
      f: V => B
  ): AsyncSeqIterator[A, B]

}

object AsyncSeqIterator {

  object cons {

    def apply[A, P, V](
        initial: => Future[A],
        convValue: A => V,
        getPos: A => Option[P],
        producer: P => Future[A]
    ): AsyncSeqIterator[A, V] = {
      new AsyncSeqIterator[A, V] {
        private lazy val computed: Future[A] = initial

        override def item()(
            implicit ec: ExecutionContext
        ): Future[A] = computed

        override def value()(
            implicit ec: ExecutionContext
        ): Future[V] = computed.map( convValue )

        override def next()(
            implicit ec: ExecutionContext
        ): Future[Option[AsyncSeqIterator[A, V]]] = {
          computed.map { computedValue =>
            getPos( computedValue ).map { pos =>
              cons(
                producer( pos ),
                convValue,
                getPos,
                producer
              )
            }
          }
        }

        override def map[B]( f: V => B ): AsyncSeqIterator[A, B] = {
          cons(
            initial,
            convValue.andThen( f ),
            getPos,
            producer
          )
        }
      }
    }
  }
}
