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

import org.latestbit.slack.morphism.client.SlackApiClientError
import org.latestbit.slack.morphism.client.streaming.impl.SlackApiScrollableReactivePublisher
import org.latestbit.slack.morphism.concurrent.AsyncSeqIterator
import org.reactivestreams.Publisher

import scala.concurrent._

/**
 * Support for batch loading remote data
 * @param initialLoader a function to initial request for data
 * @param batchLoader a function to load next batch based on previous state
 * @tparam IT batch item type
 * @tparam PT batch value type
 */
class SlackApiResponseScroller[IT, PT](
    initialLoader: () => Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]],
    batchLoader: PT => Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]]
) extends SlackApiResponseSyncScroller.LazyScalaCollectionSupport[IT, PT] {

  /**
   * Read the initial data
   *
   * @note this functions is mostly available to help to implement your own batching. If it wasn't your intention look at toAsync/SyncScroller or toPublisher
   * @return a scrollable response with a cursor position
   */
  def first(): Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]] = initialLoader()

  /**
   * Read a next batch providing a last position
   *
   * @note this functions is mostly available to help to implement your own batching. If it wasn't your intention look at toAsync/SyncScroller or toPublisher
   * @param lastPosition a cursor position
   * @return
   */
  def next( lastPosition: PT ): Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]] =
    batchLoader( lastPosition )

  /**
   * Read data as an infinite async iterator
   * @return infinite async sequence iterator
   */
  def toAsyncScroller()(
      implicit ec: ExecutionContext
  ): AsyncSeqIterator[AsyncItemType, AsyncValueType] = {
    AsyncSeqIterator.cons(
      initialLoader(), { item: AsyncItemType =>
        item.map( _.items )
      }, { item: AsyncItemType =>
        item.toOption.flatMap( _.getLatestPos )
      },
      batchLoader
    )
  }

  /**
   * Read data as a reactive publisher
   * @param maxItems - limit optionally maximum items you want to receive
   * @return reactive publisher
   */
  def toPublisher(
      maxItems: Option[Long] = None
  )( implicit ec: ExecutionContext ): Publisher[IT] =
    new SlackApiScrollableReactivePublisher( this, maxItems )

}

object SlackApiResponseScroller {}
