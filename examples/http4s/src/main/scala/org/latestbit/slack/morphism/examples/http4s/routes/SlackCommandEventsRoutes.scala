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

import cats.effect.Sync
import cats.syntax.all._
import com.typesafe.scalalogging.StrictLogging
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.client.reqresp.events.SlackApiEventMessageReply
import org.latestbit.slack.morphism.common.SlackResponseTypes
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb
import org.latestbit.slack.morphism.examples.http4s.templates.SlackSampleMessageReplyTemplateExample

class SlackCommandEventsRoutes[F[_] : Sync](
    slackApiClient: SlackApiClientT[F],
    implicit val tokensDb: SlackTokensDb[F],
    implicit val config: AppConfig
) extends StrictLogging
    with SlackEventsMiddleware {

  def routes(): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "command" => {
        slackSignedRoutes[F]( req ) {
          req.decode[UrlForm] { form =>
            ( form.getFirst( "text" ), form.getFirst( "response_url" ) ) match {
              case ( Some( text ), Some( responseUrl ) ) => {

                val commandReply = new SlackSampleMessageReplyTemplateExample(
                  text
                )

                slackApiClient.events
                  .reply(
                    response_url = responseUrl,
                    SlackApiEventMessageReply(
                      text = commandReply.renderPlainText(),
                      blocks = commandReply.renderBlocks(),
                      response_type = Some( SlackResponseTypes.Ephemeral )
                    )
                  )
                  .flatMap { resp =>
                    resp.leftMap( err => logger.error( err.getMessage() ) )
                    Ok()
                  }
              }
              case _ => {
                BadRequest()
              }
            }
          }
        }
      }
    }
  }

}
