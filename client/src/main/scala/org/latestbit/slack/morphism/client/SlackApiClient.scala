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

import cats.Monad
import cats.effect.Resource
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
   * val client = SlackApiClient.create[Future]()
   *
   * }}}
   *
   * An example for cats-effect IO:
   * {{{
   *
   * implicit val cs: ContextShift[IO] = IO.contextShift( scala.concurrent.ExecutionContext.global )
   *
   * for {
   *   backend <- AsyncHttpClientCatsBackend[IO]() // Creating an STTP backend
   *   client = SlackApiClient.build[IO]( backend ).create() // Create a Slack API client
   *   result <- client.api.test( SlackApiTestRequest() ) // call an example method inside IO monad
   * } yield result
   *
   * }}}
   *
   */
  def create[F[_] : SlackApiClientBackend.BackendType]()(
      implicit sttpBackend: SlackApiClientBackend.SttpBackendType[F]
  ) = SlackApiClientBuildOptions( sttpBackend ).create()

  /**
   * Build an instance of Slack API client with the specified options
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
  ): SlackApiClientBuildOptions[F] = SlackApiClientBuildOptions( sttpBackend )

  /**
   * Build an instance of Slack API client with the specified backend and options
   *
   * @param sttpBackend an STTP backend
   * @tparam F scala.concurrent.Future or cats.effect.IO
   * @return an instance builder
   */
  def build[F[_] : SlackApiClientBackend.BackendType](
      sttpBackend: SlackApiClientBackend.SttpBackendType[F]
  ): SlackApiClientBuildOptions[F] = {
    implicit val backend = sttpBackend
    SlackApiClientBuildOptions[F]( sttpBackend )
  }

  /**
   * Wrap a client instance into cats Resource
   * @param client a client instance
   * @tparam F scala.concurrent.Future or cats.effect.IO
   * @return a resource
   */
  def toResource[F[_] : SlackApiClientBackend.BackendType](
      client: SlackApiClientT[F]
  ): Resource[F, SlackApiClientT[F]] = {
    Resource.make( Monad[F].pure( client ) ) { client => Monad[F].pure( client.shutdown() ) }
  }

  case class SlackApiClientBuildOptions[F[_] : SlackApiClientBackend.BackendType] private[client] (
      sttpBackend: SlackApiClientBackend.SttpBackendType[F],
      throttler: SlackApiRateThrottler[F] = SlackApiRateThrottler.createEmptyThrottler[F]()
  ) {

    /**
     * Specify a throttler implementation
     * @param throttler a throttler implementation
     * @return a builder with options
     */
    def withThrottler( throttler: SlackApiRateThrottler[F] ): SlackApiClientBuildOptions[F] = copy(
      throttler = throttler
    )

    /**
     * Create a client instance with the specified options
     * @return a client instance
     */
    def create(): SlackApiClientT[F] = {
      new SlackApiClientT[F]( throttler, sttpBackend )
    }

    /**
     * Create a client as a resource
     */
    def resource(): Resource[F, SlackApiClientT[F]] = {
      toResource( create() )
    }

  }

}
