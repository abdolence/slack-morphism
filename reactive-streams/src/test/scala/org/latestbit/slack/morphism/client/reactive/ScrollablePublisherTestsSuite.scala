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

package org.latestbit.slack.morphism.client.reactive

import org.latestbit.slack.morphism.client.tests.TestScrollableResponse
import org.reactivestreams.tck.{ PublisherVerification, TestEnvironment }
import org.reactivestreams.{ Publisher, Subscriber, Subscription }
import org.scalatestplus.testng.TestNGSuiteLike
import org.testng.annotations.Test

import scala.concurrent.ExecutionContext.Implicits.global
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller

import cats.instances.future._
import scala.concurrent.Future

class ScrollablePublisherTestsSuite( env: TestEnvironment, publisherShutdownTimeout: Long )
    extends PublisherVerification[Int]( env, publisherShutdownTimeout )
    with TestNGSuiteLike {

  def this() = {
    this( new TestEnvironment( 1500 ), 1000 )
  }

  def createPublisher( elements: Long ): Publisher[Int] = {
    val scrollableResponse: SlackApiResponseScroller[Future, Int, String, TestScrollableResponse] =
      TestScrollableResponse.createTestScrollableResponse()

    scrollableResponse.toPublisher( maxItems = Some( elements ) )
  }

  override def createFailedPublisher(): Publisher[Int] =
    (s: Subscriber[_ >: Int]) => {
      s.onSubscribe( new Subscription {
        override def request( n: Long ): Unit = {
          s.onError( new Exception( "Unable to serve subscribers right now!" ) )
        }
        override def cancel(): Unit = {}
      } )
      s.onError( new Exception( "Unable to serve subscribers right now!" ) )
    }

  @Test
  def ext_required_requestWholeStream(): Unit = {
    this.activePublisherTest(
      maxElementsFromPublisher(),
      true,
      new PublisherVerification.PublisherTestRun[Int]() {

        override def run( pub: Publisher[Int] ): Unit = {
          val sub = env.newManualSubscriber( pub )
          TestScrollableResponse.testFoldedBatchesResults.foreach { testBatchRes =>
            assert(
              this.requestNextElementOrEndOfStream( pub, sub ).get() === testBatchRes,
              String.format( "Publisher %s produced wsong element", pub )
            )
          }
          sub.requestEndOfStream()
        }

        private def requestNextElementOrEndOfStream(
            pub: Publisher[Int],
            sub: TestEnvironment.ManualSubscriber[Int]
        ) =
          sub.requestNextElementOrEndOfStream(
            String.format( "Timeout while waiting for next element from Publisher %s", pub )
          )
      }
    )
  }
}
