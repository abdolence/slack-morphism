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
import org.latestbit.slack.morphism.client.reqresp.emoji._
import sttp.client._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Emoji API methods
 */
trait SlackApiEmojiClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object emoji {

    /**
     * https://api.slack.com/methods/emoji.list
     */
    def list()(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiEmojiListResponse]] = {

      http.get[SlackApiEmojiListResponse](
        "emoji.list",
        Map[String, Option[String]]()
      )
    }

  }

}
