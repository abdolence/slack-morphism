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

package org.latestbit.slack.morphism.client

import org.latestbit.slack.morphism.client.ratectrl.SlackApiRateThrottler

object SlackApiClient {

  /**
   * Create an instance of Slack API client for the specified backend kind (Future|cats-effect IO, etc)
   *
   * @param sttpBackend an implicitly defined STTP backend
   * @tparam F scala.concurrent.Future or cats.effect.IO
   * @return an instance of Slack API client
   *
   * For example:
   *
   * {{{
   * // For Future:
   * implicit val sttpBackend = AsyncHttpClientFutureBackend()
   *
   * SlackApiClient.create()
   * }}}
   *
   * {{{
   *
   * // For IO:
   * implicit val cs: ContextShift[IO] = IO.contextShift( scala.concurrent.ExecutionContext.global )
   *
   * AsyncHttpClientCatsBackend[IO]()
   *       .flatMap { implicit backEnd =>
   *         for {
   *           client <- IO( SlackApiClient.create[IO]() )
   *           testResult <- client.api.test( SlackApiTestRequest() )
   *         }
   *         yield
   *            testResult
   *       }
   *
   * SlackApiClient.create()
   * }}}
   *
   */
  def create[F[_] : SlackApiClientBackend.BackendType]()(
      implicit sttpBackend: SlackApiClientBackend.SttpBackendType[F]
  ) = SlackApiClientBuildOptions( sttpBackend ).create()

  /**
   * Building an instance of Slack API client with the specified options
   *
   * For example:
   *
   * {{{
   *
   * SlackApiClient
   *  .build(AsyncHttpClientFutureBackend())
   *  .withThrottler( SlackApiRateThrottler.createStandardThrottler() )
   *  .create()
   * }}}
   *
   * @param sttpBackend an implicitly defined STTP backend
   * @tparam F scala.concurrent.Future or cats.effect.IO
   * @return an instance builder
   */
  def build[F[_] : SlackApiClientBackend.BackendType](
      implicit sttpBackend: SlackApiClientBackend.SttpBackendType[F]
  ): SlackApiClientBuilder[F] = SlackApiClientBuildOptions( sttpBackend )

  trait SlackApiClientBuilder[F[_]] {
    def withThrottler( throttler: SlackApiRateThrottler[F] ): SlackApiClientBuilder[F]
    def create(): SlackApiClientT[F]
  }

  private case class SlackApiClientBuildOptions[F[_] : SlackApiClientBackend.BackendType](
      sttpBackend: SlackApiClientBackend.SttpBackendType[F],
      throttler: SlackApiRateThrottler[F] = SlackApiRateThrottler.createEmptyThrottler[F]()
  ) extends SlackApiClientBuilder[F] {

    override def withThrottler( throttler: SlackApiRateThrottler[F] ): SlackApiClientBuilder[F] = copy(
      throttler = throttler
    )

    override def create(): SlackApiClientT[F] = {
      implicit val backend = sttpBackend
      new SlackApiClientT[F]( throttler )
    }

  }

}
