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

class SlackApiResponseScroller[IT, PT](
    initialLoader: () => Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]],
    batchLoader: PT => Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]]
) extends SlackApiResponseSyncScroller.LazyScalaCollectionSupport[IT, PT] {

  def first(): Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]] = initialLoader()

  def next( lastPosition: PT ): Future[Either[SlackApiClientError, SlackApiScrollableResponse[IT, PT]]] =
    batchLoader( lastPosition )

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

  def toPublisher(
      maxItems: Option[Long] = None
  )( implicit ec: ExecutionContext ): Publisher[IT] =
    new SlackApiScrollableReactivePublisher( this, maxItems )

}

object SlackApiResponseScroller {}
