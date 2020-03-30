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

import org.latestbit.slack.morphism.client.streaming._
import fs2.Stream

package object fs2s {

  implicit class SlackClientFS2Scroller[F[_] : SlackApiClientBackend.BackendType, IT, PT, SR <: SlackApiScrollableResponse[
    IT,
    PT
  ]](
      scroller: SlackApiResponseScroller[F, IT, PT, SR]
  ) {

    private def iterateEvalF(
        current: F[Either[SlackApiClientError, SR]]
    ): Stream[F, SR] = {
      Stream.eval( current ).flatMap {
        case Right( item ) => {
          Stream( item ) ++
            item.getLatestPos
              .map { pos => iterateEvalF( scroller.next( pos ) ) }
              .getOrElse( Stream.empty )
        }
        case Left( err ) => {
          Stream.raiseError[F]( err )
        }

      }

    }

    /**
     * Read data as a FS2 stream
     */
    def toFs2Scroller(): Stream[F, IT] = {
      iterateEvalF( scroller.first() )
        .flatMap( res => Stream.emits( res.items ) )
    }
  }

}
