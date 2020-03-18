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

trait SlackApiRateThrottler {

  def throttle[RS](
      uri: Uri,
      tier: Option[Int],
      apiToken: Option[SlackApiToken],
      methodMaxDelay: Option[FiniteDuration]
  )(
      request: () => Future[Either[SlackApiClientError, RS]]
  ): Future[Either[SlackApiClientError, RS]]

  def shutdown(): Unit
}

object SlackApiRateThrottler {

  case object Empty extends SlackApiRateThrottler {
    override def shutdown(): Unit = {}

    override def throttle[RS](
        uri: Uri,
        tier: Option[Int],
        apiToken: Option[SlackApiToken],
        methodMaxDelay: Option[FiniteDuration]
    )(
        request: () => Future[Either[SlackApiClientError, RS]]
    ): Future[Either[SlackApiClientError, RS]] = request()
  }

  def createStandardThrottler(): SlackApiRateThrottler = {
    createStandardThrottler( SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS )
  }

  def createStandardThrottler( params: SlackApiRateControlParams ): SlackApiRateThrottler = {
    new StandardRateThrottlerImpl(
      params
    )
  }

  def createEmptyThrottler(): SlackApiRateThrottler = SlackApiRateThrottler.Empty

}
