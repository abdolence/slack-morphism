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

package org.latestbit.slack.morphism.client

import io.circe.JsonObject
import org.latestbit.slack.morphism.client.reqresp.test._
import org.latestbit.slack.morphism.client.streaming._
import org.scalatest.flatspec.AsyncFlatSpec

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

class ScrollableTestsSuite extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport {

  import org.latestbit.slack.morphism.client.tests.TestScrollableResponse._

  val scrollableResponse = createTestScrollableResponse()

  "A scroller" should "be able to work as a sync Stream" in {
    scrollableResponse.toSyncScroller( 5.seconds ) map {
      case Right( scroller ) => {
        val loadedList = scroller.toList
        assert( loadedList === testFoldedBatchesResults )
      }
      case Left( err ) => fail( err )
    }

  }

  it should "be able to work as a async iterator" in {
    val asyncIterator = scrollableResponse.toAsyncScroller()
    asyncIterator.foldLeft( List[Int]() ) {
      case ( wholeList, futureRes ) =>
        futureRes.map( wholeList ++ _ ).getOrElse( wholeList )
    } map { foldedResList =>
      assert( foldedResList === testFoldedBatchesResults )
    }

  }

}
