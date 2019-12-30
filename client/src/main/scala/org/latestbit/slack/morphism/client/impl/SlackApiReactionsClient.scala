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

import io.circe.generic.auto._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.models.reactions._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack test API methods
 */
trait SlackApiReactionsClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object reactions {

    /**
     * https://api.slack.com/methods/reactions.add
     */
    def add( req: SlackApiReactionsAddRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiReactionsAddResponse]] = {

      protectedSlackHttpApiPost[SlackApiReactionsAddRequest, SlackApiReactionsAddResponse](
        "reactions.add",
        req
      )
    }

    /**
     * https://api.slack.com/methods/reactions.get
     */
    def get( req: SlackApiReactionsGetRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiReactionsGetResponse]] = {

      protectedSlackHttpApiGet[SlackApiReactionsGetResponse](
        "reactions.get",
        Map(
          "channel" -> Option( req.channel ),
          "timestamp" -> Option( req.timestamp ),
          "full" -> req.full.map( _.toString() )
        )
      )
    }

    /**
     * https://api.slack.com/methods/reactions.list
     */
    def list( req: SlackApiReactionsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiReactionsListResponse]] = {

      protectedSlackHttpApiGet[SlackApiReactionsListResponse](
        "reactions.list",
        Map(
          "cursor" -> req.cursor,
          "full" -> req.full.map( _.toString() ),
          "limit" -> req.limit.map( _.toString() ),
          "user" -> req.user
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/reactions.list
     */
    def listScroller( req: SlackApiReactionsListRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): SlackApiResponseScroller[SlackApiReactionsListItem, String] = {
      new SlackApiResponseScroller[SlackApiReactionsListItem, String](
        initialLoader = { () =>
          list( req )
        },
        batchLoader = { cursor =>
          list(
            SlackApiReactionsListRequest(
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/reactions.remove
     */
    def remove( req: SlackApiReactionsRemoveRequest )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpBackend[Future, Nothing, NothingT],
        ec: ExecutionContext
    ): Future[Either[SlackApiError, SlackApiReactionsRemoveResponse]] = {

      protectedSlackHttpApiPost[SlackApiReactionsRemoveRequest, SlackApiReactionsRemoveResponse](
        "reactions.remove",
        req
      )
    }

  }

}
