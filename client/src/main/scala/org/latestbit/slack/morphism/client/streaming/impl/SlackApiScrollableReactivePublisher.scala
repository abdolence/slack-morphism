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

package org.latestbit.slack.morphism.client.streaming.impl

import cats.Monad
import cats.effect.IO
import org.latestbit.slack.morphism.client.streaming.{ SlackApiResponseScroller, SlackApiScrollableResponse }
import org.reactivestreams.{ Publisher, Subscriber, Subscription }

import scala.concurrent.ExecutionContext

class SlackApiScrollableReactivePublisher[F[_] : Monad, IT, PT, SR <: SlackApiScrollableResponse[IT, PT]](
    scrollableResponse: SlackApiResponseScroller[F, IT, PT, SR],
    maxItems: Option[Long] = None
)( implicit ec: ExecutionContext )
    extends Publisher[IT] {
  private implicit val ctxShift = IO.contextShift( ec )

  private final class SlackApiScrollableSubscription( subscriber: Subscriber[_ >: IT] ) extends Subscription {

    private val commandsChannel = new SlackApiScrollableSubscriptionCommandChannel[F, IT, PT, SR](
      subscriber,
      scrollableResponse,
      maxItems
    )

    override def request( n: Long ): Unit = {
      if (n < 0) {
        subscriber.onError(
          new IllegalArgumentException( "3.9. Non-positive subscription request." )
        )
      } else if (n == 0) {
        subscriber.onError(
          new IllegalArgumentException( "3.9. Requested zero number of elements" )
        )
      } else {
        commandsChannel.enqueue(
          SlackApiScrollableSubscriptionCommandChannel.RequestElements( n )
        )
      }
    }

    override def cancel(): Unit = {
      commandsChannel.shutdown()
    }

    private[impl] def start(): Unit = {
      commandsChannel.start()
    }
  }

  override def subscribe( subscriber: Subscriber[_ >: IT] ): Unit = {
    val subscription = new SlackApiScrollableSubscription( subscriber )
    subscriber.onSubscribe( subscription )
    subscription.start()
  }
}
