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

package org.latestbit.slack.morphism.client.impl

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.codecs.implicits._
import sttp.client3._
import cats.implicits._
import org.latestbit.slack.morphism.client.reqresp.events._

/**
 * Support for Slack test API methods
 */
trait SlackApiEventsCallbackClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object events {

    /**
     * Reply an event message response using response_url from Slack Events
     * https://api.slack.com/interactivity/handling#message_responses
     *
     * @param response_url a url from Slack Event
     * @param reply reply to an event
     */
    def reply( response_url: String, reply: SlackApiEventMessageReply )( implicit
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiEventMessageReplyResponse]] = {

      sendSlackRequest[SlackApiEventMessageReplyResponse](
        encodePostBody( createSlackHttpApiRequest(), reply ).post( uri"${response_url}" )
      ).map( handleSlackEmptyRes( SlackApiEventMessageReplyResponse() ) )
    }

  }

}
