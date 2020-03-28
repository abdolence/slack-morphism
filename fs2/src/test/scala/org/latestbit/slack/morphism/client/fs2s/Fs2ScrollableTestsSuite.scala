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

package org.latestbit.slack.morphism.client.fs2s

import org.latestbit.slack.morphism.client.SlackApiClientTestsSuiteSupport
import org.scalatest.flatspec.AnyFlatSpec

class Fs2ScrollableTestsSuite extends AnyFlatSpec with SlackApiClientTestsSuiteSupport {

  import org.latestbit.slack.morphism.client.tests.TestScrollableResponse._

  "A scroller" should "be able to work as a FS2 Stream" in {
    val testScroller = createIOTestScrollableResponse()

    assert(
      testScroller.toFs2Scroller().compile.toList.unsafeRunSync() == testFoldedBatchesResults
    )
  }

}
