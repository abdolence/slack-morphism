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

import java.time.{ LocalDateTime, ZoneId }

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.{ SlackApiClientT, SlackApiToken }
import org.latestbit.slack.morphism.client.reqresp.chat._
import org.latestbit.slack.morphism.client.reqresp.conversations.SlackApiConversationsHistoryRequest
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsPublishRequest
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.http4s.templates._
import org.latestbit.slack.morphism.views.SlackHomeView

class SlackPushEventsRoutes[F[_] : Sync](
    slackApiClient: SlackApiClientT[F],
    implicit val tokensDb: SlackTokensDb[F],
    implicit val config: AppConfig
) extends StrictLogging
    with SlackEventsMiddleware
    with CirceCodecs {

  def routes(): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    def onPushEvent( event: SlackPushEvent ) = event match {
      case urlVerEv: SlackUrlVerificationEvent => {
        logger.info( s"Received a challenge request:\n${urlVerEv.challenge}" )
        Ok()
      }
      case callbackEvent: SlackEventCallback => {
        extractSlackWorkspaceToken[F]( callbackEvent.team_id ) { implicit slackApiToken =>
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
              logger.info(
                s"Received a user message '${msg.text.getOrElse( "-" )}' in ${msg.channel.getOrElse( "-" )}"
              )
              sendReplyToMsg( msg )
            }

            case unknownBody: SlackEventCallbackBody => {
              logger.warn( s"We don't handle this callback event we received in this example: ${unknownBody}" )
              Ok()
            }
          }
        }
      }

      case pushEvent: SlackPushEvent => {
        logger.warn( s"We don't handle this push event we received in this example: ${pushEvent}" )
        Ok()
      }

    }

    def sendReplyToMsg( msg: SlackUserMessage )( implicit apiToken: SlackApiToken ) = {
      val template = new SlackSampleMessageReplyTemplateExample( msg.text.getOrElse( "" ) )
      slackApiClient.chat
        .postMessage(
          SlackApiChatPostMessageRequest(
            channel = msg.channel.get,
            text = template.renderPlainText(),
            blocks = template.renderBlocks()
          )
        )
        .flatMap {
          case Right( resp ) => {
            logger.info( s"Sent a reply message: ${resp}" )
            Ok()
          }
          case Left( err ) => {
            logger.error( s"Unable to sent a reply message: ", err )
            InternalServerError()
          }
        }
    }

    def generateLatestNews(): List[SlackHomeNewsItem] = {
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

    def updateHomeTab( userId: String )( implicit apiToken: SlackApiToken ) = {
      slackApiClient.views
        .publish(
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
        .flatMap {
          case Right( publishResp ) => {
            logger.info( s"Home view for ${userId} has been published: ${publishResp}" )
            Ok()
          }
          case Left( err ) => {
            logger.error( s"Unable to update home view for ${userId}", err )
            InternalServerError()
          }
        }
    }

    def sendWelcomeMessage( channelId: String, userId: String )(
        implicit slackApiToken: SlackApiToken
    ): F[Response[F]] = {
      EitherT(
        slackApiClient.conversations
          .history(
            SlackApiConversationsHistoryRequest( channel = channelId, limit = Some( 5 ) )
          )
      ).map { channelHistoryResp =>
          if (channelHistoryResp.messages.isEmpty) {
            val template = new SlackWelcomeMessageTemplateExample( userId )
            slackApiClient.chat
              .postMessage(
                SlackApiChatPostMessageRequest(
                  channel = channelId,
                  text = template.renderPlainText(),
                  blocks = template.renderBlocks()
                )
              )
              .map { publishResp =>
                logger.info( s"Home view for ${userId} has been published: ${publishResp}" )
                ()
              }
          } else {
            ()
          }
        }
        .value
        .flatMap {
          case Right( _ ) => {
            Ok()
          }
          case Left( err ) => {
            logger.error( s"Home view update for ${userId} error: ${err}" )
            InternalServerError()
          }
        }

    }

    HttpRoutes.of[F] {
      case req @ POST -> Root / "push" => {
        slackSignedRoutes[F, SlackPushEvent]( req )( onPushEvent )
      }
    }

  }

}
