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

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.latestbit.slack.morphism.client.SlackApiClientT
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb

class SlackOAuthRoutes[F[_] : Sync](
    slackApiClient: SlackApiClientT[F],
    tokensDb: SlackTokensDb[F],
    config: AppConfig
) extends StrictLogging {

  private val SLACK_AUTH_URL_V2 = uri"https://slack.com/oauth/v2/authorize"

  def routes(): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    val basePath = Path( "auth" )

    object QueryParamsMatchers {
      object QueryCodeParam extends OptionalQueryParamDecoderMatcher[String]( "code" )
      object ErrorCodeParam extends OptionalQueryParamDecoderMatcher[String]( "error" )
    }

    import QueryParamsMatchers._

    HttpRoutes.of[F] {
      case GET -> Root / basePath / "install" => {
        val baseParams: Map[String, String] = List[( String, Option[String] )](
          "client_id" -> Option( config.slackAppConfig.clientId ),
          "scope" -> Option( config.slackAppConfig.botScope ),
          "redirect_uri" -> config.slackAppConfig.redirectUrl
        ).flatMap { case ( k, v ) => v.map( k -> _ ) }.toMap

        for {
          resp <- TemporaryRedirect( Location( SLACK_AUTH_URL_V2.withQueryParams( baseParams ) ) )
        } yield resp
      }

      case GET -> Root / basePath / "callback" :? QueryCodeParam( code ) +& ErrorCodeParam( error ) => {
        ( code, error ) match {
          case ( Some( oauthCode ), _ ) => {
            logger.info( s"Received OAuth access code: ${oauthCode}" )
            EitherT(
              slackApiClient.oauth.v2.access(
                clientId = config.slackAppConfig.clientId,
                clientSecret = config.slackAppConfig.clientSecret,
                code = oauthCode,
                redirectUri = config.slackAppConfig.redirectUrl
              )
            ).map { tokens =>
                logger.info( s"Received OAuth access tokens: ${tokens}" )
                tokensDb
                  .insertToken(
                    teamId = tokens.team.id,
                    SlackTokensDb.TokenRecord(
                      tokenType = tokens.token_type,
                      scope = tokens.scope,
                      tokenValue = tokens.access_token,
                      userId = tokens.bot_user_id.getOrElse( tokens.authed_user.id )
                    )
                  )
              }
              .value
              .flatMap {
                case Right( _ ) => Ok()
                case Left( err ) => {
                  logger.info( s"OAuth access error : ${err}" )
                  InternalServerError()
                }
              }
          }
          case ( _, Some( error ) ) => {
            logger.info( s"OAuth error code: ${error}" )
            Ok()
          }
          case _ => {
            logger.error( s"No OAuth code or error provided?" )
            InternalServerError()
          }
        }
      }
    }
  }

}
