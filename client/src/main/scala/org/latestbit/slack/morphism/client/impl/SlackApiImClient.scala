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
import org.latestbit.slack.morphism.client.reqresp.im._
import org.latestbit.slack.morphism.messages.SlackMessage
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.common.SlackChannelInfo
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack IM API methods
 */
trait SlackApiImClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object im {

    /**
     * https://api.slack.com/methods/im.close
     */
    def close( req: SlackApiImCloseRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiImCloseResponse]] = {

      http.post[SlackApiImCloseRequest, SlackApiImCloseResponse](
        "im.close",
        req
      )
    }

    /**
     * https://api.slack.com/methods/im.history
     */
    def history( req: SlackApiImHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiImHistoryResponse]] = {

      http.get[SlackApiImHistoryResponse](
        "im.history",
        Map(
          "channel" -> Option( req.channel ),
          "count" -> req.count.map( _.toString() ),
          "inclusive" -> req.inclusive.map( _.toString() ),
          "latest" -> req.latest,
          "oldest" -> req.oldest,
          "unreads" -> req.unreads.map( _.toString() )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/im.history
     */
    def historyScroller( req: SlackApiImHistoryRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackMessage, String, SlackApiImHistoryResponse] = {
      new SlackApiResponseScroller[F, SlackMessage, String, SlackApiImHistoryResponse](
        initialLoader = { () => history( req ) },
        batchLoader = { pos =>
          history(
            SlackApiImHistoryRequest(
              channel = req.channel,
              oldest = Some( pos ),
              count = req.count
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/im.list
     */
    def list( req: SlackApiImListRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiImListResponse]] = {

      http.get[SlackApiImListResponse](
        "im.list",
        Map(
          "cursor" -> req.cursor,
          "limit" -> req.limit.map( _.toString() )
        )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/im.list
     */
    def listScroller( req: SlackApiImListRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackChannelInfo, String, SlackApiImListResponse] = {
      new SlackApiResponseScroller[F, SlackChannelInfo, String, SlackApiImListResponse](
        initialLoader = { () => list( req ) },
        batchLoader = { cursor =>
          list(
            SlackApiImListRequest(
              cursor = Some( cursor ),
              limit = req.limit
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/im.mark
     */
    def mark( req: SlackApiImMarkRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiImMarkResponse]] = {

      http.post[SlackApiImMarkRequest, SlackApiImMarkResponse](
        "im.mark",
        req
      )
    }

    /**
     * https://api.slack.com/methods/im.open
     */
    def open( req: SlackApiImOpenRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiImOpenResponse]] = {

      http.post[SlackApiImOpenRequest, SlackApiImOpenResponse](
        "im.open",
        req
      )
    }

    /**
     * https://api.slack.com/methods/im.replies
     */
    def replies( req: SlackApiImRepliesRequest )(
        implicit slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiImRepliesResponse]] = {

      http.get[SlackApiImRepliesResponse](
        "im.replies",
        Map(
          "channel" -> Option( req.channel ),
          "thread_ts" -> Option( req.thread_ts )
        )
      )
    }

  }

}
