package org.latestbit.slack.morphism.examples.http4s

import cats.implicits._
import fs2.Stream
import cats.effect._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.blaze.server._

import scala.concurrent.ExecutionContext.global
import org.http4s.server.middleware.Logger
import org.latestbit.slack.morphism.client.SlackApiClient
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.http4s.routes._
import sttp.client3.http4s.Http4sBackend

object Http4sServer {

  def stream(
      config: AppConfig
  ): Stream[IO, Nothing] = {
    for {
      httpClient <- BlazeClientBuilder[IO].stream
      slackApiClient <- Stream.resource(
                          SlackApiClient
                            .build[IO](
                              Http4sBackend.usingClient( httpClient )
                            )
                            .resource()
                        )
      tokensDb <- Stream.resource( SlackTokensDb.open[IO]( config ) )

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
                  new SlackOAuthRoutes[IO]( slackApiClient, tokensDb, config ).routes() <+>
                    new SlackPushEventsRoutes[IO]( slackApiClient, tokensDb, config ).routes() <+>
                    new SlackInteractionEventsRoutes[IO]( slackApiClient, tokensDb, config ).routes() <+>
                    new SlackCommandEventsRoutes[IO]( slackApiClient, tokensDb, config ).routes()
                ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp( logHeaders = true, logBody = true )( httpApp )

      exitCode <- BlazeServerBuilder[IO]
                    .bindHttp( config.httpServerPort, config.httpServerHost )
                    .withHttpApp( finalHttpApp )
                    .serve
    } yield exitCode
  }.drain
}
