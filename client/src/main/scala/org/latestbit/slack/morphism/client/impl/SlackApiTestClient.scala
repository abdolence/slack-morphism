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
import org.latestbit.slack.morphism.client.reqresp.test._

import scala.concurrent.{ ExecutionContext, Future }
import org.latestbit.slack.morphism.codecs.implicits._

/**
 * Support for Slack test API methods
 */
trait SlackApiTestClient extends SlackApiHttpProtocolSupport {

  object api {

    /**
     * https://api.slack.com/methods/api.test
     */
    def test( req: SlackApiTestRequest )(
        implicit slackApiToken: SlackApiUserToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiTestResponse]] = {

      http.post[SlackApiTestRequest, SlackApiTestResponse](
        "api.test",
        req
      )
    }

  }

}
