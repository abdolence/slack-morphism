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
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.client.reqresp.reactions._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.codecs.implicits._
import org.latestbit.slack.morphism.common.SlackCursorId

/**
 * Support for Slack test API methods
 */
trait SlackApiReactionsClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object reactions {

    /**
     * https://api.slack.com/methods/reactions.add
     */
    def add( req: SlackApiReactionsAddRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiReactionsAddResponse]] = {

      http.post[SlackApiReactionsAddRequest, SlackApiReactionsAddResponse](
        "reactions.add",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/reactions.get
     */
    def get( req: SlackApiReactionsGetRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiReactionsGetResponse]] = {

      http.get[SlackApiReactionsGetResponse](
        "reactions.get",
        Map(
          "channel"   -> Option( req.channel.value ),
          "timestamp" -> Option( req.timestamp ),
          "full"      -> req.full.map( _.toString() )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/reactions.list
     */
    def list( req: SlackApiReactionsListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiReactionsListResponse]] = {

      http.get[SlackApiReactionsListResponse](
        "reactions.list",
        Map(
          "cursor" -> req.cursor.map( _.value ),
          "full"   -> req.full.map( _.toString() ),
          "limit"  -> req.limit.map( _.toString() ),
          "user"   -> req.user.map( _.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

    /**
     * Scrolling support for https://api.slack.com/methods/reactions.list
     */
    def listScroller( req: SlackApiReactionsListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackApiReactionsListItem, SlackCursorId, SlackApiReactionsListResponse] = {
      new SlackApiResponseScroller[F, SlackApiReactionsListItem, SlackCursorId, SlackApiReactionsListResponse](
        initialLoader = { () => list( req ) },
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
    def remove( req: SlackApiReactionsRemoveRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiReactionsRemoveResponse]] = {

      http.post[SlackApiReactionsRemoveRequest, SlackApiReactionsRemoveResponse](
        "reactions.remove",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) )
      )
    }

  }

}
