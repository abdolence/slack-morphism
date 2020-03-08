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
import org.latestbit.slack.morphism.client.reqresp.chat.{ SlackApiPostEventReply, SlackApiPostWebHookRequest }
import org.latestbit.slack.morphism.client.reqresp.views.SlackApiViewsOpenRequest
import org.latestbit.slack.morphism.client.{ SlackApiClient, SlackApiToken }
import org.latestbit.slack.morphism.common.SlackResponseTypes
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.examples.akka.AppConfig
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.akka.templates._

import scala.concurrent.ExecutionContext

class SlackCommandEventsRoute(
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
    path( "command" ) {
      post {
        extractSlackSignedRequest { requestBody =>
          logger.debug( s"Received a command: ${requestBody}" )
          formFields(
            "team_id",
            "team_domain".?,
            "channel_id",
            "channel_name".?,
            "user_id",
            "user_name".?,
            "command",
            "text".?,
            "response_url",
            "trigger_id"
          ) {
            case (
                team_id,
                team_domain,
                channel_id,
                channel_name,
                user_id,
                user_name,
                command,
                text,
                response_url,
                trigger_id
                ) =>
              // Sending additional reply using response_url
              val commandReply = new SlackSampleMessageReplyTemplateExample(
                text.getOrElse( "" )
              )

              slackApiClient.chat
                .postEventReply(
                  response_url = response_url,
                  SlackApiPostEventReply(
                    text = commandReply.renderPlainText(),
                    blocks = commandReply.renderBlocks(),
                    response_type = Some( SlackResponseTypes.EPHEMERAL )
                  )
                )
                .foreach {
                  case Right( resp ) => {
                    logger.info( s"Sent a reply message: ${resp}" )

                  }
                  case Left( err ) => {
                    logger.error( s"Unable to sent a reply message: ", err )
                  }
                }

              // Sending work in progress message
              completeWithJson(
                SlackApiPostEventReply(
                  text = "Working on it..."
                )
              )

          }
        }
      }
    }
  }

}
