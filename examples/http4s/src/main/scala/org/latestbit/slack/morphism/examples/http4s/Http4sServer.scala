package org.latestbit.slack.morphism.examples.http4s

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Timer }
import cats.syntax.all._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.latestbit.slack.morphism.client.SlackApiClient
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.http4s.routes._
import sttp.client.http4s.Http4sBackend

import scala.concurrent.ExecutionContext.global

object Http4sServer {

  def stream[F[_] : ConcurrentEffect](
      config: AppConfig
  )( implicit T: Timer[F], C: ContextShift[F] ): Stream[F, Nothing] = {
    for {
      httpClient <- BlazeClientBuilder[F]( global ).stream
      blocker    <- Stream.resource( Blocker[F] )
      slackApiClient <- Stream.resource(
                          SlackApiClient
                            .build[F](
                              Http4sBackend.usingClient( httpClient, blocker )
                            )
                            .resource()
                        )
      tokensDb <- Stream.resource( SlackTokensDb.open[F]( config ) )

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
                    new SlackOAuthRoutes[F]( slackApiClient, tokensDb, config ).routes() <+>
                      new SlackPushEventsRoutes[F]( slackApiClient, tokensDb, config ).routes() <+>
                      new SlackInteractionEventsRoutes[F]( slackApiClient, tokensDb, config ).routes() <+>
                      new SlackCommandEventsRoutes[F]( slackApiClient, tokensDb, config ).routes()
                ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp( logHeaders = true, logBody = true )( httpApp )

      exitCode <- BlazeServerBuilder[F]( global )
                    .bindHttp( config.httpServerPort, config.httpServerHost )
                    .withHttpApp( finalHttpApp )
                    .serve
    } yield exitCode
  }.drain
}
