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

import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.reactivestreams.{ Publisher, Subscriber, Subscription }

import scala.concurrent.ExecutionContext

class SlackApiScrollableReactivePublisher[IT, PT](
    scrollableResponse: SlackApiResponseScroller[IT, PT],
    maxItems: Option[Long] = None
)( implicit ec: ExecutionContext )
    extends Publisher[IT] {

  private class SlackApiScrollableSubscription( subscriber: Subscriber[_ >: IT] )
      extends Subscription {

    val commandsProcessor = new SlackApiScrollableSubscriptionCommandProcessor[IT, PT](
      subscriber,
      scrollableResponse,
      maxItems
    )

    override def request( n: Long ): Unit = {
      if (n < 0) {
        subscriber.onError(
          new IllegalArgumentException( "Subscriber requested negative number of elements" )
        )
      } else if (n == 0) {
        subscriber.onError(
          new IllegalArgumentException( "Subscriber requested zero number of elements" )
        )
      } else {
        commandsProcessor.enqueueCommand(
          SlackApiScrollableSubscriptionCommandProcessor.RequestElements( n )
        )
      }
    }

    override def cancel(): Unit = {
      commandsProcessor.shutdown()
    }
  }

  override def subscribe( subscriber: Subscriber[_ >: IT] ): Unit = {
    subscriber.onSubscribe( new SlackApiScrollableSubscription( subscriber ) )
  }
}
