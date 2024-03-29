/*
 * Copyright 2020 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

import org.latestbit.slack.morphism.client.reactive.impl.SlackApiScrollableReactivePublisher
import org.latestbit.slack.morphism.client.streaming.{ SlackApiResponseScroller, SlackApiScrollableResponse }
import org.reactivestreams.Publisher

package object reactive {

  implicit class SlackClientReactiveStreamsScroller[F[
      _
  ] : SlackApiClientBackend.BackendType, IT, PT, SR <: SlackApiScrollableResponse[
    IT,
    PT
  ]](
      scroller: SlackApiResponseScroller[F, IT, PT, SR]
  ) {

    /**
     * Read data as a reactive publisher
     * @param maxItems
     *   - limit optionally maximum items you want to receive
     * @return
     *   reactive publisher
     */
    def toPublisher(
        maxItems: Option[Long] = None
    ): Publisher[IT] =
      new SlackApiScrollableReactivePublisher[F, IT, PT, SR]( scroller, maxItems )
  }
}
