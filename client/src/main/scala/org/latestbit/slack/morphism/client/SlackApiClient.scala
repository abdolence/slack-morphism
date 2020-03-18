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

import org.latestbit.slack.morphism.client.impl._
import org.latestbit.slack.morphism.client.ratectrl._

/**
 * Slack API client
 */
class SlackApiClient(
    protected override val throttler: SlackApiRateThrottler = SlackApiRateThrottler.createEmptyThrottler()
)( implicit protected override val sttpBackend: SlackApiClientBackend.SttpFutureBackendType )
    extends SlackApiHttpRateControlSupport
    with SlackApiOAuthClient
    with SlackApiTestClient
    with SlackApiAppsClient
    with SlackApiAuthClient
    with SlackApiBotsClient
    with SlackApiChannelsClient
    with SlackApiChatClient
    with SlackApiConversationsClient
    with SlackApiDndClient
    with SlackApiEmojiClient
    with SlackApiImClient
    with SlackApiPinsClient
    with SlackApiReactionsClient
    with SlackApiTeamClient
    with SlackApiUsersClient
    with SlackApiViewsClient {

  /**
   * Release all resources allocated by a client.
   * Depends on your configuration, the Slack API client may allocate threads for example.
   */
  def shutdown(): Unit = {
    throttler.shutdown()
  }

}
