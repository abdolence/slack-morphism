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
import org.latestbit.slack.morphism.client.reqresp.views._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Support for Slack Views API methods
 */
trait SlackApiViewsClient extends SlackApiHttpProtocolSupport { self: SlackApiClient =>

  object views {

    /**
     * https://api.slack.com/methods/views.open
     */
    def open( req: SlackApiViewsOpenRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiViewsOpenResponse]] = {

      http.post[SlackApiViewsOpenRequest, SlackApiViewsOpenResponse](
        "views.open",
        req
      )
    }

    /**
     * https://api.slack.com/methods/views.publish
     */
    def publish( req: SlackApiViewsPublishRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiViewsPublishResponse]] = {

      http.post[SlackApiViewsPublishRequest, SlackApiViewsPublishResponse](
        "views.publish",
        req
      )
    }

    /**
     * https://api.slack.com/methods/views.push
     */
    def push( req: SlackApiViewsPushRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiViewsPushResponse]] = {

      http.post[SlackApiViewsPushRequest, SlackApiViewsPushResponse](
        "views.push",
        req
      )
    }

    /**
     * https://api.slack.com/methods/views.update
     */
    def update( req: SlackApiViewsUpdateRequest )(
        implicit slackApiToken: SlackApiToken,
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, SlackApiViewsUpdateResponse]] = {

      http.post[SlackApiViewsUpdateRequest, SlackApiViewsUpdateResponse](
        "views.update",
        req
      )
    }

  }

}
