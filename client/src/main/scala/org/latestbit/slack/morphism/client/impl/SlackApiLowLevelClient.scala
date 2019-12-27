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

import io.circe.{ Decoder, Encoder }
import org.latestbit.slack.morphism.client._
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Access to low-level Slack HTTP protocol
 */
trait SlackApiLowLevelClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object http {

    /**
     * Make HTTP GET to Slack API
     * @param methodUri a relative method uri (like 'api.test')
     * @param params HTTP GET URL params
     * @return Decoded from JSON result
     */
    def get[RS]( methodUri: String, params: Map[String, Option[String]] = Map() )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        decoder: Decoder[RS],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, RS]] = {
      protectedSlackHttpApiGet[RS](
        methodUri,
        params
      )
    }

    /**
     * Make HTTP GET to Slack API
     * @param methodUri a relative method uri (like 'api.test')
     * @param req a request model to encode to JSON
     * @return Decoded from JSON result
     */
    def post[RQ, RS]( methodUri: String, req: RQ )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        encoder: Encoder[RQ],
        decoder: Decoder[RS],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, RS]] = {
      protectedSlackHttpApiPost[RQ, RS](
        methodUri,
        req
      )
    }

  }

}
