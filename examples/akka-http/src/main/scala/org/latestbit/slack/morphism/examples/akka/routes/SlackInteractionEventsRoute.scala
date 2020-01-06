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

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream.typed.scaladsl.ActorMaterializer
import com.typesafe.scalalogging._
import io.circe.parser._
import org.latestbit.slack.morphism.client.{ SlackApiClient, SlackApiToken }
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsOpenRequest
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.akka.AppConfig
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.akka.templates._

import scala.concurrent.ExecutionContext

class SlackInteractionEventsRoute(
    implicit ctx: ActorContext[_],
    materializer: ActorMaterializer,
    config: AppConfig,
    slackApiClient: SlackApiClient,
    slackTokensDb: ActorRef[SlackTokensDb.Command]
) extends StrictLogging
    with AkkaHttpServerRoutesSupport
    with Directives {

  implicit val ec: ExecutionContext = ctx.system.executionContext

  val routes: Route = {
    path( "interaction" ) {
      post {
        extractSlackSignedRequest { requestBody =>
          formField( "payload" ) { payload =>
            decode[SlackInteractionEvent]( payload ) match {
              case Right( event ) => onEvent( event )
              case Left( ex ) => {
                logger.error( s"Can't decode push event from Slack: ${ex.toString}\n${requestBody}" )
                complete( StatusCodes.BadRequest )
              }
            }
          }
        }
      }
    }
  }

  private def showDummyModal( triggerId: String )( implicit slackApiToken: SlackApiToken ) = {
    val modalTemplateExample = new SlackModalTemplateExample()
    onSuccess(
      slackApiClient.views.open(
        SlackApiViewsOpenRequest(
          trigger_id = triggerId,
          view = modalTemplateExample.toModalView()
        )
      )
    ) {
      case Right( resp ) => {
        logger.info( s"Modal view has been opened: ${resp}" )
        complete( StatusCodes.OK )
      }
      case Left( err ) => {
        logger.error( s"Unable to open modal view", err )
        complete( StatusCodes.InternalServerError )
      }
    }
  }

  def onEvent( event: SlackInteractionEvent ): Route = {
    routeWithSlackApiToken( event.team.id ) { implicit slackApiToken =>
      event match {
        case blockActionEvent: SlackInteractionBlockActionEvent => {
          logger.info( s"Received a block action event: ${blockActionEvent}" )
          showDummyModal( blockActionEvent.trigger_id )
        }
        case messageActionEvent: SlackInteractionMessageActionEvent => {
          logger.info( s"Received a message action event: ${messageActionEvent}" )
          showDummyModal( messageActionEvent.trigger_id )
        }
        case actionSubmissionEvent: SlackInteractionViewSubmissionEvent => {
          actionSubmissionEvent.view.stateParams.state.foreach { state =>
            logger.info( s"Received action submission state: ${state}" )
          }
          complete(
            StatusCodes.OK,
            HttpEntity(
              ContentTypes.`text/plain(UTF-8)`,
              ""
            )
          )
        }
        case interactionEvent: SlackInteractionEvent => {
          logger.warn( s"We don't handle this interaction in this example: ${interactionEvent}" )
          complete( StatusCodes.OK )
        }
      }
    }
  }

}
