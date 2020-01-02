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
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.typed.scaladsl.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser._
import org.latestbit.slack.morphism.client.SlackApiClient
import org.latestbit.slack.morphism.client.impl.SlackApiHttpProtocolSupport
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.akka.AppConfig
import sttp.client.{ NothingT, SttpBackend }

import scala.concurrent.{ ExecutionContext, Future }

class SlackOAuthRoutes(
    implicit ctx: ActorContext[_],
    materializer: ActorMaterializer,
    config: AppConfig,
    slackApiClient: SlackApiClient
) extends LazyLogging
    with AkkaHttpServerRoutesSupport
    with Directives {

  implicit val ec: ExecutionContext = ctx.system.executionContext

  val routes: Route = {
    pathPrefix( "auth" ) {
      get {
        path( "callback" ) {
          parameters( "code".?, "error".?, "state".? ) {
            case ( code, error, state ) =>
              ( code, error ) match {
                case ( Some( oauthCode ), _ ) => {
                  logger.info( s"Received OAuth access code: ${oauthCode}" )
                  onSuccess(
                    slackApiClient.oauth.v2.access(
                      clientId = config.slackAppConfig.clientId,
                      clientSecret = config.slackAppConfig.clientSecret,
                      code = oauthCode,
                      redirectUri = config.slackAppConfig.redirectUrl
                    )
                  ) {
                    case Right( accessCode ) => {
                      logger.info( s"Received OAuth access code: ${accessCode}" )
                      complete( StatusCodes.OK )
                    }
                    case Left( err ) => {
                      logger.info( s"OAuth access error : ${err}" )
                      complete( StatusCodes.InternalServerError )
                    }
                  }
                }
                case ( _, Some( error ) ) => {
                  logger.info( s"OAuth error code: ${error}" )
                  complete( StatusCodes.OK )
                }
                case _ => {
                  logger.error( s"No OAuth code or error provided?" )
                  complete( StatusCodes.InternalServerError )
                }
              }
          }
        }
      }
    }
  }

}
