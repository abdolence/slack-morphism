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

package org.latestbit.slack.morphism.client.reactive.impl

import cats.Monad
import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.unsafe.IORuntime
import org.latestbit.slack.morphism.client.streaming.{ SlackApiResponseScroller, SlackApiScrollableResponse }
import org.reactivestreams.{ Publisher, Subscriber, Subscription }

class SlackApiScrollableReactivePublisher[F[_] : Monad, IT, PT, SR <: SlackApiScrollableResponse[IT, PT]](
    scrollableResponse: SlackApiResponseScroller[F, IT, PT, SR],
    maxItems: Option[Long] = None
) extends Publisher[IT] {
  private implicit val ioRuntime: IORuntime = cats.effect.unsafe.IORuntime.global

  private final class SlackApiScrollableSubscription(
      queue: SlackApiScrollableSubscriptionCommandChannel.CommandQueue,
      subscriber: Subscriber[_ >: IT]
  ) extends Subscription {

    private val commandsChannel = new SlackApiScrollableSubscriptionCommandChannel[F, IT, PT, SR](
      queue,
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

    private[impl] def startConsuming(): IO[Unit] = {
      commandsChannel.start()
    }
  }

  override def subscribe( srcSubscriber: Subscriber[_ >: IT] ): Unit = {
    Option( srcSubscriber ) match {
      case Some( subscriber ) =>
        ( for {
          commandQueue <- Queue.unbounded[IO, SlackApiScrollableSubscriptionCommandChannel.Command]
          subscription = new SlackApiScrollableSubscription( commandQueue, subscriber )
          _            = subscriber.onSubscribe( subscription )
          _ <- subscription.startConsuming()
        } yield () ).unsafeRunAndForget()
      case None => throw new NullPointerException( "Subscriber can't be null" )
    }
  }
}
