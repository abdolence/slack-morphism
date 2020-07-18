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

package org.latestbit.slack.morphism.examples.http4s.routes

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.reqresp.views._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.common.SlackTriggerId
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.http4s.templates._

class SlackInteractionEventsRoutes[F[_] : Sync](
    slackApiClient: SlackApiClientT[F],
    implicit val tokensDb: SlackTokensDb[F],
    implicit val config: AppConfig
) extends StrictLogging
    with SlackEventsMiddleware
    with CirceCodecs {

  def routes(): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    def onEvent( event: SlackInteractionEvent ): F[Response[F]] = {
      extractSlackWorkspaceToken[F]( event.team.id ) { implicit apiToken =>
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
            Ok( "" ) // "" is required here by Slack
          }
          case interactionEvent: SlackInteractionEvent => {
            logger.warn( s"We don't handle this interaction in this example: ${interactionEvent}" )
            Ok()
          }
        }
      }
    }

    def showDummyModal( triggerId: SlackTriggerId )( implicit slackApiToken: SlackApiToken ) = {
      val modalTemplateExample = new SlackModalTemplateExample()
      slackApiClient.views
        .open(
          SlackApiViewsOpenRequest(
            trigger_id = triggerId,
            view = modalTemplateExample.toModalView()
          )
        )
        .flatMap {
          case Right( resp ) => {
            logger.info( s"Modal view has been opened: ${resp}" )
            Ok()
          }
          case Left( err ) => {
            logger.error( s"Unable to open modal view", err )
            InternalServerError()
          }
        }
    }

    HttpRoutes.of[F] {
      case req @ POST -> Root / "interaction" => {
        slackSignedRoutes[F]( req ) {
          req.decode[UrlForm] { form =>
            OptionT
              .fromOption[F](
                form.getFirst( "payload" )
              )
              .flatMap( decodeJson[F, SlackInteractionEvent] )
              .map { event => onEvent( event ) }
              .value
              .flatMap( _.getOrElse( BadRequest() ) )
          }

        }
      }
    }

  }

}
