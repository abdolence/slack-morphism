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
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Decoder
import io.circe.parser._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.latestbit.slack.morphism.client.SlackApiToken
import org.latestbit.slack.morphism.common.SlackTeamId
import org.latestbit.slack.morphism.events.signature._
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import org.latestbit.slack.morphism.examples.http4s.db.SlackTokensDb
import org.typelevel.ci.CIString

trait SlackEventsMiddleware extends StrictLogging {

  private val slackSignatureVerifier = new SlackEventSignatureVerifier()

  private def verifySlackSignatureRequest[F[_] : Concurrent : Async](
      config: AppConfig,
      req: Request[F]
  ): F[Either[SlackSignatureVerificationError, String]] = {
    req.bodyText.compile
      .fold( "" )( _ ++ _ )
      .flatMap { body =>
        Async[F].delay(
          (
            req.headers.get( CIString(SlackEventSignatureVerifier.HttpHeaderNames.SignedHash) ),
            req.headers.get( CIString(SlackEventSignatureVerifier.HttpHeaderNames.SignedTimestamp) )
          ) match {
            case ( Some( receivedHash ), Some( signedTimestamp ) ) => {
              slackSignatureVerifier
                .verify(
                  config.slackAppConfig.signingSecret,
                  receivedHash.head.value,
                  signedTimestamp.head.value,
                  body
                )
                .map { _ => body }
            }
            case _ => SlackAbsentSignatureError( "Absent HTTP headers required for a Slack signature" ).asLeft
          }
        )
      }
  }

  private def decodeVerifiedSlackEventBody[F[_] : Concurrent : Async](
      config: AppConfig,
      req: Request[F]
  ) = {
    OptionT(
      verifySlackSignatureRequest[F]( config, req )
        .flatMap {
          case Right( body ) => Concurrent[F].pure( body.some )
          case Left( err ) =>
            Concurrent[F]
              .delay( logger.error( "Error: {}", err ) )
              .map { _ => Option.empty[String] }
        }
    )
  }

  protected def decodeJson[F[_] : Sync, J]( body: String )( implicit decoder: Decoder[J] ): OptionT[F, J] = {
    OptionT(
      Sync[F].delay {
        decode[J]( body ) match {
          case Right( decoded ) => decoded.some
          case Left( err ) => {
            logger.error( s"Decode error: {}", err )
            Option.empty[J]
          }
        }
      }
    )
  }

  protected def slackSignedRoutes[F[_] : Concurrent : Async](
      req: Request[F]
  )( resp: => F[Response[F]] )( implicit config: AppConfig ): F[Response[F]] = {
    decodeVerifiedSlackEventBody[F]( config, req )
      .flatMapF { _ => resp.map( _.some ) }
      .getOrElseF(
        Concurrent[F].pure(
          Response[F]( status = Forbidden )
        )
      )
  }

  protected def slackSignedRoutes[F[_] : Concurrent : Async, J](
      req: Request[F]
  )( resp: J => F[Response[F]] )( implicit config: AppConfig, decoder: Decoder[J] ): F[Response[F]] = {
    decodeVerifiedSlackEventBody[F]( config, req )
      .flatMap( decodeJson[F, J] )
      .flatMapF { decoded => resp( decoded ).map( _.some ) }
      .getOrElseF(
        Concurrent[F].pure(
          Response[F]( status = Forbidden )
        )
      )
  }

  protected def extractSlackWorkspaceToken[F[_] : Concurrent : Async](
      teamId: SlackTeamId
  )(
      resp: SlackApiToken => F[Response[F]]
  )( implicit tokensDb: SlackTokensDb[F] ): F[Response[F]] =
    OptionT(
      tokensDb
        .readTokens( teamId )
        .map { tokensRecord =>
          tokensRecord
            .flatMap( record =>
              record.tokens.lastOption.map { lastToken =>
                SlackApiToken.createFrom(
                  tokenType = lastToken.tokenType,
                  tokenValue = lastToken.tokenValue,
                  scope = Some( lastToken.scope ),
                  teamId = Some( teamId )
                )
              }
            )
        }
    ).flatMapF { token => resp( token ).map( _.some ) }.getOrElseF {
      Concurrent[F]
        .delay(
          logger.warn( "Token absent for: {}", teamId )
        )
        .map( _ =>
          Response[F](
            status = Ok
          )
        )
    }

}
