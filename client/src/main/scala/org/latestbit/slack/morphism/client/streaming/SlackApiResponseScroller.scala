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

package org.latestbit.slack.morphism.client.streaming

import org.latestbit.slack.morphism.client.SlackApiError
import org.latestbit.slack.morphism.client.streaming.impl.SlackApiScrollableReactivePublisher
import org.latestbit.slack.morphism.concurrent.AsyncSeqIterator
import org.reactivestreams.Publisher

import scala.collection.AbstractIterator
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.concurrent.{ Await, ExecutionContext, Future }

class SlackApiResponseScroller[IT, PT](
    initialLoader: () => Future[Either[SlackApiError, SlackApiScrollableResponse[IT, PT]]],
    batchLoader: PT => Future[Either[SlackApiError, SlackApiScrollableResponse[IT, PT]]]
) {

  def first(): Future[Either[SlackApiError, SlackApiScrollableResponse[IT, PT]]] = initialLoader()

  def next( lastPosition: PT ): Future[Either[SlackApiError, SlackApiScrollableResponse[IT, PT]]] =
    batchLoader( lastPosition )

  object lazyload {
    type SyncStreamType = Stream[IT]
    type AsyncItemType = Either[SlackApiError, SlackApiScrollableResponse[IT, PT]]
    type AsyncValueType = Either[SlackApiError, Iterable[IT]]

    def toSyncScroller(
        scrollerTimeout: FiniteDuration = 60.seconds
    )( implicit ec: ExecutionContext ): Future[Either[SlackApiError, SyncStreamType]] = {

      def loadNext( scrollableResp: SlackApiScrollableResponse[IT, PT] ): SyncStreamType = {
        val loadedStream = scrollableResp.items.toStream
        scrollableResp.getLatestPos
          .map { latestPos =>
            lazy val scrollNext: SyncStreamType =
              Await
                .result(
                  batchLoader( latestPos ),
                  scrollerTimeout
                )
                .toOption
                .map( loadNext )
                .getOrElse( Stream.empty )

            loadedStream #::: scrollNext
          }
          .getOrElse( loadedStream )
      }

      initialLoader().map( _.map( loadNext ) )
    }

    def toAsyncScroller()(
        implicit ec: ExecutionContext
    ): AsyncSeqIterator[AsyncItemType, AsyncValueType] = {
      AsyncSeqIterator.cons(
        first(), { item: AsyncItemType =>
          item.map( _.items )
        }, { item: AsyncItemType =>
          item.toOption.flatMap( _.getLatestPos )
        },
        batchLoader
      )
    }

  }

  object reactive {

    def toPublisher(
        maxItems: Option[Long] = None
    )( implicit ec: ExecutionContext ): Publisher[IT] =
      new SlackApiScrollableReactivePublisher( SlackApiResponseScroller.this, maxItems )

  }

}

object SlackApiResponseScroller {}
