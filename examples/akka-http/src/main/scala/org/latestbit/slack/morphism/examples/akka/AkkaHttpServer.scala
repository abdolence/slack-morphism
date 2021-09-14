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

package org.latestbit.slack.morphism.examples.akka

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.typesafe.scalalogging._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.akka.routes._
import sttp.client3.akkahttp.AkkaHttpBackend

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util._
import cats.instances.future._
import org.latestbit.slack.morphism.examples.akka.config.AppConfig

object AkkaHttpServer extends StrictLogging {
  sealed trait Command
  case class Start( config: AppConfig ) extends Command
  case class Stop()                     extends Command

  val run: Behavior[Command] = runBehavior( None )

  private case class HttpServerState(
      httpBinding: Future[Http.ServerBinding],
      tokensDbRef: ActorRef[SlackTokensDb.Command]
  )

  private def runBehavior( serverState: Option[HttpServerState] ): Behavior[Command] =
    Behaviors.setup { implicit context =>
      implicit val system = context.system

      implicit val ec: ExecutionContextExecutor = context.system.executionContext

      Behaviors.receiveMessage {
        case Start( config ) => {
          logger.info(
            s"Starting routes on ${config.httpServerHost}:${config.httpServerPort}"
          )
          implicit val appConfig = config
          implicit val akkaSttpBackend: SlackApiClientBackend.SttpBackendType[Future] =
            AkkaHttpBackend.usingActorSystem( context.system.toClassic )
          implicit val slackApiClient = SlackApiClient.create()

          implicit val tokensDbRef = context.spawnAnonymous( SlackTokensDb.run )

          tokensDbRef ! SlackTokensDb.OpenDb( config )

          val slackPushEventsRoute        = new SlackPushEventsRoutes()
          val slackOAuthRoute             = new SlackOAuthRoutes()
          val slackInteractionEventsRoute = new SlackInteractionEventsRoutes()
          val slackCommandEventsRoute     = new SlackCommandEventsRoutes()

          val allRoutes: Route = {
            ignoreTrailingSlash {
              path( "test" ) {
                get {
                  complete( StatusCodes.OK )
                }
              } ~
                slackPushEventsRoute.routes ~
                slackOAuthRoute.routes ~
                slackInteractionEventsRoute.routes ~
                slackCommandEventsRoute.routes

            }
          }

          val binding = Http()
            .newServerAt(
              config.httpServerHost,
              config.httpServerPort
            )
            .bindFlow(
              allRoutes
            )

          binding onComplete {
            case Success( bound ) =>
              logger.info(
                s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
              )
            case Failure( ex ) =>
              logger.error( s"Server could not start: ", ex )
              context.system.terminate()
          }

          runBehavior(
            Some(
              HttpServerState(
                httpBinding = binding,
                tokensDbRef = tokensDbRef
              )
            )
          )
        }

        case Stop() => {
          serverState.foreach { state =>
            state.httpBinding.foreach { binding =>
              logger.info( "Stopping http server" )
              binding.terminate( 5.seconds )
            }
            state.tokensDbRef ! SlackTokensDb.Close()
          }
          Behaviors.stopped
        }
      }
    }

}
