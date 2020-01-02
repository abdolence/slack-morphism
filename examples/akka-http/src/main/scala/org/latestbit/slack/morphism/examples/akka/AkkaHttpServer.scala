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

import akka.Done
import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.stream.typed.scaladsl._
import com.typesafe.scalalogging._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util._

object AkkaHttpServer extends LazyLogging {
  sealed trait Command
  case class Start( config: AppConfig ) extends Command
  case class Stop() extends Command

  val run: Behavior[Command] = runBehavior( None )

  private case class HttpServerState(
      httpBinding: Future[Http.ServerBinding]
  )

  private def runBehavior( serverState: Option[HttpServerState] ): Behavior[Command] =
    Behaviors.setup { implicit context =>
      implicit val system = context.system
      implicit val classicSystem = context.system.toClassic
      implicit val materializer = ActorMaterializer()
      implicit val ec: ExecutionContextExecutor = context.system.executionContext
      val httpServerRoutes = new AkkaHttpServerRoutes()

      Behaviors.receiveMessage {
        case Start( config ) => {
          logger.info(
            s"Starting routes on ${config.httpServerHost}:${config.httpServerPort}"
          )

          val binding = Http().bindAndHandle(
            httpServerRoutes.createRoutes(),
            config.httpServerHost,
            config.httpServerPort
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
                httpBinding = binding
              )
            )
          )
        }

        case Stop() => {
          serverState.foreach { state =>
            state.httpBinding.foreach { binding =>
              binding.terminate( 5.seconds )
            }
          }
          Behavior.stopped
        }
      }
    }

}
