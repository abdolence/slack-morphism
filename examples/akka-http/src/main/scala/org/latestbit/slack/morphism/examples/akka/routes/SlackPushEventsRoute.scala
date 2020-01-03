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

package org.latestbit.slack.morphism.examples.akka.routes

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream.typed.scaladsl.ActorMaterializer
import com.typesafe.scalalogging._
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.akka.AppConfig

import scala.concurrent.ExecutionContext
import io.circe.parser._

class SlackPushEventsRoute( implicit ctx: ActorContext[_], materializer: ActorMaterializer, config: AppConfig )
    extends StrictLogging
    with AkkaHttpServerRoutesSupport
    with Directives {

  implicit val ec: ExecutionContext = ctx.system.executionContext

  val routes: Route = {
    path( "push" ) {
      post {
        extractSlackSignedRequest { requestBody =>
          decode[SlackPushEvent]( requestBody ) match {
            case Right( event ) => onEvent( event )
            case Left( ex ) => {
              logger.error( "Can't decode push event from Slack.", ex )
              complete( StatusCodes.BadRequest )
            }
          }
        }
      }
    }
  }

  def onEvent( event: SlackPushEvent ): Route = event match {
    case ev: SlackUrlVerificationEvent => {
      logger.info( s"Received a challenge request:\n${ev.challenge}" )
      complete(
        StatusCodes.OK,
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          ev.challenge
        )
      )
    }
    case ev: SlackPushEvent => {
      logger.warn( s"Unsupported event received: ${ev}" )
      complete( StatusCodes.OK )
    }
  }

}
