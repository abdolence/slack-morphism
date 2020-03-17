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

package org.latestbit.slack.morphism.client.ratectl

import org.latestbit.slack.morphism.client.ratectl.impl.StandardRateThrottler
import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiToken }
import sttp.model.Uri

import scala.concurrent.Future

trait RateThrottler {

  def throttle[RS]( apiMethodUri: Option[Uri], apiTier: Option[Int], apiToken: Option[SlackApiToken] )(
      request: () => Future[Either[SlackApiClientError, RS]]
  ): Future[Either[SlackApiClientError, RS]]

  def shutdown(): Unit
}

object RateThrottler {

  case object Empty extends RateThrottler {
    override def shutdown(): Unit = {}

    override def throttle[RS]( apiMethodUri: Option[Uri], apiTier: Option[Int], apiToken: Option[SlackApiToken] )(
        request: () => Future[Either[SlackApiClientError, RS]]
    ): Future[Either[SlackApiClientError, RS]] = request()
  }

  def createStandardThrottler(): RateThrottler = {
    createStandardThrottler( RateControlParams.SlackStandardLimits.DEFAULT_PARAMS )
  }

  def createStandardThrottler( params: RateControlParams ): RateThrottler = {
    new StandardRateThrottler( params )
  }

  def createEmptyThrottler(): RateThrottler = RateThrottler.Empty

}
