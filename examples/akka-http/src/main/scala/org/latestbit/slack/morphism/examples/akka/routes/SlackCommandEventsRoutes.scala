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
import akka.http.scaladsl.server._
import com.typesafe.scalalogging._
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.common.SlackResponseTypes
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.akka.templates._

import scala.concurrent.{ ExecutionContext, Future }
import cats.instances.future._
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.examples.akka.config.AppConfig

class SlackCommandEventsRoutes( implicit
    ctx: ActorContext[_],
    config: AppConfig,
    slackApiClient: SlackApiClientT[Future],
    slackTokensDb: ActorRef[SlackTokensDb.Command]
) extends StrictLogging
    with AkkaHttpServerRoutesSupport
    with Directives {

  implicit val ec: ExecutionContext = ctx.system.executionContext
  implicit val actorSystem          = ctx.system

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
                  _,
                  _,
                  _,
                  _,
                  _,
                  _,
                  _,
                  text,
                  response_url,
                  _
                ) =>
              // Sending additional reply using response_url
              val commandReply = new SlackSampleMessageReplyTemplateExample(
                text.getOrElse( "" )
              )

              slackApiClient.events
                .reply(
                  response_url = response_url,
                  SlackApiEventMessageReply(
                    text = commandReply.renderPlainText(),
                    blocks = commandReply.renderBlocks(),
                    response_type = Some( SlackResponseTypes.Ephemeral )
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
                SlackApiEventMessageReply(
                  text = "Working on it..."
                )
              )

          }
        }
      }
    }
  }

}
