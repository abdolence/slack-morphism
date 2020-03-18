/*
 * Copyright 2020 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

package org.latestbit.slack.morphism.client.ratectrl

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl.impl._
import sttp.model.Uri

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
 * Slack API call rate throttler
 */
trait SlackApiRateThrottler {

  /**
   * Throttle a Slack API call if necessary
   *
   * @param uri Slack API method URI
   * @param apiToken An API token
   * @param methodRateControl method rate control parameters
   * @param request An API async request
   * @tparam RS Response data type
   * @return a future either directly request or delayed request
   */
  def throttle[RS](
      uri: Uri,
      apiToken: Option[SlackApiToken],
      methodRateControl: Option[SlackApiMethodRateControlParams]
  )(
      request: () => Future[Either[SlackApiClientError, RS]]
  ): Future[Either[SlackApiClientError, RS]]

  /**
   * Release all throttle resources (threads, etc)
   */
  def shutdown(): Unit
}

object SlackApiRateThrottler {

  private case object Empty extends SlackApiRateThrottler {
    override def shutdown(): Unit = {}

    override def throttle[RS](
        uri: Uri,
        apiToken: Option[SlackApiToken],
        methodRateControl: Option[SlackApiMethodRateControlParams]
    )(
        request: () => Future[Either[SlackApiClientError, RS]]
    ): Future[Either[SlackApiClientError, RS]] = request()
  }

  /**
   * Create a Slack API throttler with default parameters
   * accordingly to https://api.slack.com/docs/rate-limits
   * @return a throttler implementation
   */
  def createStandardThrottler(): SlackApiRateThrottler = {
    createStandardThrottler( SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS )
  }

  /**
   * Create a Slack API throttler with the specified parameters
   * @return a throttler implementation
   */
  def createStandardThrottler( params: SlackApiRateControlParams ): SlackApiRateThrottler = {
    new StandardRateThrottlerImpl(
      params
    )
  }

  /**
   * Create an empty throttler (no throttling control)
   * @return an empty throttler implementation
   */
  def createEmptyThrottler(): SlackApiRateThrottler = SlackApiRateThrottler.Empty

}
