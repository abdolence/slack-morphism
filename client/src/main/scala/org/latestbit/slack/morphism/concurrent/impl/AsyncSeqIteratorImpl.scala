/*
 * Copyright 2020 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

package org.latestbit.slack.morphism.concurrent.impl

import cats.Monad
import cats.syntax.all._
import org.latestbit.slack.morphism.concurrent.AsyncSeqIterator

private[concurrent] final class AsyncSeqIteratorImpl[F[_] : Monad, I, A, P] private[concurrent] (
    initial: => F[I],
    toValue: I => Option[A],
    getPos: I => Option[P],
    producer: P => F[I]
) extends AsyncSeqIterator[F, I, A] {
  private lazy val computed: F[I] = initial

  override def item(): F[I] = computed

  override def value(): F[Option[A]] = computed.map( toValue )

  override def next(): F[Option[AsyncSeqIterator[F, I, A]]] = {
    computed.map { computedValue =>
      getPos( computedValue ).map { pos =>
        new AsyncSeqIteratorImpl(
          producer( pos ),
          toValue,
          getPos,
          producer
        )
      }
    }
  }

  override def map[B]( f: A => B ): AsyncSeqIterator[F, I, B] = {
    new AsyncSeqIteratorImpl[F, I, B, P](
      initial,
      toValue.andThen( _.map( f ) ),
      getPos,
      producer
    )
  }

  override def foldLeft[B]( initial: B )( f: ( B, A ) => B ): F[B] = {
    value().flatMap { currentValueOpt =>
      val folded =
        currentValueOpt.map { currentValue => f( initial, currentValue ) }.getOrElse( initial )

      next().flatMap {
        case Some( nextIterator ) => {
          nextIterator.foldLeft( folded )( f )
        }
        case _ => {
          Monad[F].pure( folded )
        }
      }
    }
  }

  override def foreach[U]( f: A => U ): Unit = {
    value().flatMap { currentValueOpt =>
      currentValueOpt.foreach( f )
      next().map {
        case Some( nextIter ) => {
          nextIter.foreach( f )
        }
        case _ => ()
      }
    }
    ()
  }

  override def filter( f: A => Boolean ): AsyncSeqIterator[F, I, A] =
    new AsyncSeqIteratorImpl(
      initial,
      toValue.andThen( _.filter( f ) ),
      getPos,
      producer
    )
}
