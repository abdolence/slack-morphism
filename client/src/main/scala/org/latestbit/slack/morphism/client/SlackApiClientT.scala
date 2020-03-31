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

import org.latestbit.slack.morphism.client.SlackApiClientBackend._
import org.latestbit.slack.morphism.client.impl._
import org.latestbit.slack.morphism.client.ratectrl._

/**
 * Slack API client
 */
class SlackApiClientT[F[_] : SlackApiClientBackend.BackendType] private[client] (
    override protected val throttler: SlackApiRateThrottler[F],
    override protected val sttpBackend: SttpBackendType[F]
) extends SlackApiHttpRateControlSupport[F]
    with SlackApiOAuthClient[F]
    with SlackApiTestClient[F]
    with SlackApiAppsClient[F]
    with SlackApiAuthClient[F]
    with SlackApiBotsClient[F]
    with SlackApiChannelsClient[F]
    with SlackApiChatClient[F]
    with SlackApiConversationsClient[F]
    with SlackApiDndClient[F]
    with SlackApiEmojiClient[F]
    with SlackApiImClient[F]
    with SlackApiPinsClient[F]
    with SlackApiReactionsClient[F]
    with SlackApiTeamClient[F]
    with SlackApiUsersClient[F]
    with SlackApiViewsClient[F]
    with SlackApiEventsCallbackClient[F] {

  /**
   * Release all resources allocated by a client.
   * Depends on your configuration, the Slack API client may allocate threads for example.
   */
  def shutdown(): Unit = {
    throttler.shutdown()
  }

  /**
   * Auxiliary function to help in for-comprehension with tokens,
   * where you still can't define implicit vals at the moment:
   *
   * {{{
   *  for {
   *     ...
   *     result <- client.withToken( readToken )( implicit token => _.api.test( SlackApiTestRequest() ) )
   *     ...
   *  }
   * }}}
   *
   * @param token a Slack API token
   * @param request request requires a token
   * @return result of execution
   */
  def withToken[T <: SlackApiToken, RS]( token: T )(
      request: T => SlackApiClientT[F] => F[Either[SlackApiClientError, RS]]
  ): F[Either[SlackApiClientError, RS]] = {
    request( token )( this )
  }

}
