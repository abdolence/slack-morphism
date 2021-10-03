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

import org.latestbit.slack.morphism.client.{ SlackApiClientBackend, SlackApiClientError }
import org.latestbit.slack.morphism.concurrent.AsyncSeqIterator

/**
 * Support for batch loading remote data
 * @param initialLoader
 *   a function to initial request for data
 * @param batchLoader
 *   a function to load next batch based on previous state
 * @tparam IT
 *   batch item type
 * @tparam PT
 *   batch value type
 */
class SlackApiResponseScroller[F[_] : SlackApiClientBackend.BackendType, IT, PT, SR <: SlackApiScrollableResponse[
  IT,
  PT
]](
    initialLoader: () => F[Either[SlackApiClientError, SR]],
    batchLoader: PT => F[Either[SlackApiClientError, SR]]
) extends LazyScalaCollectionSupport[F, IT, PT, SR] {

  type AsyncItemType  = Either[SlackApiClientError, SR]
  type AsyncValueType = Either[SlackApiClientError, Iterable[IT]]

  /**
   * Read the initial data
   *
   * @note
   *   this functions is mostly available to help to implement your own batching. If it wasn't your intention look at
   *   toAsync/SyncScroller or toPublisher
   * @return
   *   a scrollable response with a cursor position
   */
  def first(): F[Either[SlackApiClientError, SR]] = initialLoader()

  /**
   * Read a next batch providing a last position
   *
   * @note
   *   this functions is mostly available to help to implement your own batching. If it wasn't your intention look at
   *   toAsync/SyncScroller or toPublisher
   * @param lastPosition
   *   a cursor position
   * @return
   */
  def next( lastPosition: PT ): F[Either[SlackApiClientError, SR]] =
    batchLoader( lastPosition )

  /**
   * Read data as an infinite async iterator of batches
   * @return
   *   infinite async sequence iterator
   */
  def toAsyncScroller(): AsyncSeqIterator[F, AsyncItemType, AsyncValueType] = {
    AsyncSeqIterator.cons(
      initialLoader(),
      _.map( _.items ),
      _.toOption.flatMap( _.getLatestPos ),
      batchLoader
    )
  }

}
