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

import java.time._

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream.typed.scaladsl.ActorMaterializer
import com.typesafe.scalalogging._
import org.latestbit.slack.morphism.events._

import scala.concurrent.{ ExecutionContext, Future }
import io.circe.parser._
import org.latestbit.slack.morphism.client.reqresp.chat.SlackApiChatPostMessageRequest
import org.latestbit.slack.morphism.client.reqresp.conversations._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsPublishRequest
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.akka.templates._
import org.latestbit.slack.morphism.views.SlackHomeView
import cats.instances.future._
import org.latestbit.slack.morphism.examples.akka.config.AppConfig

class SlackPushEventsRoutes(
    implicit ctx: ActorContext[_],
    materializer: ActorMaterializer,
    config: AppConfig,
    slackApiClient: SlackApiClientT[Future],
    slackTokensDb: ActorRef[SlackTokensDb.Command]
) extends StrictLogging
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
              logger.error( s"Can't decode push event from Slack: ${ex.toString}\n${requestBody}" )
              complete( StatusCodes.BadRequest )
            }
          }
        }
      }
    }
  }

  private def generateLatestNews(): List[SlackHomeNewsItem] = {
    List(
      SlackHomeNewsItem(
        title = "Google claimed quantum supremacy in 2019 — and sparked controversy",
        body =
          "In October, researchers from Google claimed to have achieved a milestone known as quantum supremacy.\nThey had created the first quantum computer that could perform a calculation that is impossible for a standard computer.",
        published = LocalDateTime.of(2019, 12, 16, 10, 20, 0 ).atZone( ZoneId.systemDefault() ).toInstant
      ),
      SlackHomeNewsItem(
        title = "Quantum jitter lets heat travel across a vacuum",
        body = "A new experiment shows that quantum fluctuations permit heat to bridge empty space.",
        published = LocalDateTime.of(2019, 12, 11, 10, 20, 0 ).atZone( ZoneId.systemDefault() ).toInstant
      )
    )
  }

  private def updateHomeTab( userId: String )( implicit slackApiToken: SlackApiToken ) = {
    onSuccess(
      slackApiClient.views.publish(
        SlackApiViewsPublishRequest(
          user_id = userId,
          view = SlackHomeView(
            blocks = new SlackHomeTabBlocksTemplateExample(
              latestNews = generateLatestNews(),
              userId = userId
            ).renderBlocks()
          )
        )
      )
    ) {
      case Right( publishResp ) => {
        logger.info( s"Home view for ${userId} has been published: ${publishResp}" )
        complete( StatusCodes.OK )
      }
      case Left( err ) => {
        logger.error( s"Unable to update home view for ${userId}", err )
        complete( StatusCodes.InternalServerError )
      }
    }
  }

  private def sendWelcomeMessage( channelId: String, userId: String )(
      implicit slackApiToken: SlackApiToken
  ): Route = {
    onComplete(
      slackApiClient.conversations
        .historyScroller(
          SlackApiConversationsHistoryRequest( channel = channelId, limit = Some( 5 ) )
        )
        .toSyncScroller()
        .flatMap {
          case Right( channelHistory ) => {
            if (channelHistory.isEmpty) {
              val template = new SlackWelcomeMessageTemplateExample( userId )
              slackApiClient.chat
                .postMessage(
                  SlackApiChatPostMessageRequest(
                    channel = channelId,
                    text = template.renderPlainText(),
                    blocks = template.renderBlocks()
                  )
                )
                .map {
                  case Right( publishResp ) => {
                    logger.info( s"Home view for ${userId} has been published: ${publishResp}" )
                    StatusCodes.OK
                  }
                  case Left( err ) => {
                    logger.error( s"Unable to update home view for ${userId}", err )
                    StatusCodes.InternalServerError
                  }
                }
            } else {
              Future.successful(
                StatusCodes.OK
              )
            }
          }
          case Left( err ) => {
            logger.error( s"Unable to load channel ${channelId} history for ${userId}", err )
            Future.successful(
              StatusCodes.InternalServerError
            )
          }
        }
    ) { statusCode => complete( statusCode ) }
  }

  def onEvent( event: SlackPushEvent ): Route = event match {
    case urlVerEv: SlackUrlVerificationEvent => {
      logger.info( s"Received a challenge request:\n${urlVerEv.challenge}" )
      complete(
        StatusCodes.OK,
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          urlVerEv.challenge
        )
      )
    }
    case callbackEvent: SlackEventCallback => {
      routeWithSlackApiToken( callbackEvent.team_id ) { implicit slackApiToken =>
        callbackEvent.event match {
          case body: SlackAppHomeOpenedEvent => {
            logger.info( s"User opened home: ${body}" )

            if (body.tab == "home")
              updateHomeTab( body.user )
            else {
              sendWelcomeMessage( body.channel, body.user )
            }
          }
          case msg: SlackUserMessage => {
            logger.info( s"Received a user message '${msg.text.getOrElse( "-" )}' in ${msg.channel.getOrElse( "-" )}" )
            val template = new SlackSampleMessageReplyTemplateExample( msg.text.getOrElse( "" ) )
            onSuccess(
              slackApiClient.chat
                .postMessage(
                  SlackApiChatPostMessageRequest(
                    channel = msg.channel.get,
                    text = template.renderPlainText(),
                    blocks = template.renderBlocks()
                  )
                )
            ) {
              case Right( resp ) => {
                logger.info( s"Sent a reply message: ${resp}" )
                complete( StatusCodes.OK )
              }
              case Left( err ) => {
                logger.error( s"Unable to sent a reply message: ", err )
                complete( StatusCodes.InternalServerError )
              }
            }
          }
          case unknownBody: SlackEventCallbackBody => {
            logger.warn( s"We don't handle this callback event we received in this example: ${unknownBody}" )
            complete( StatusCodes.OK )
          }
        }
      }
    }

    case pushEvent: SlackPushEvent => {
      logger.warn( s"We don't handle this push event we received in this example: ${pushEvent}" )
      complete( StatusCodes.OK )
    }
  }

}
