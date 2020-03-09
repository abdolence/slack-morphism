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

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.stream.typed.scaladsl.ActorMaterializer
import akka.util.{ ByteString, Timeout }
import io.circe.Encoder
import io.circe.syntax._
import org.latestbit.slack.morphism.client.SlackApiToken
import org.latestbit.slack.morphism.events.signature.SlackEventSignatureVerifier
import org.latestbit.slack.morphism.examples.akka.AppConfig
import org.latestbit.slack.morphism.examples.akka.db.SlackTokensDb

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import cats.Functor
import cats.instances.option._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging

trait AkkaHttpServerRoutesSupport extends org.latestbit.slack.morphism.codecs.CirceCodecs with StrictLogging {

  private final val signatureVerifier = new SlackEventSignatureVerifier()

  protected def httpEntityToString(
      entity: HttpEntity,
      charset: HttpCharset = HttpCharsets.`UTF-8`
  )( implicit ec: ExecutionContext, materializer: ActorMaterializer ): Future[String] = {
    entity.dataBytes.runFold( ByteString() )( _ ++ _ ).map { bs => bs.decodeString( charset.value ) }
  }

  protected def completeWithJson[T]( response: T )( implicit encoder: Encoder.AsObject[T] ) = {
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    complete(
      response.asJson
    )
  }

  def extractSlackSignedRequest( route: String => Route, charset: HttpCharset = HttpCharsets.`UTF-8` )(
      implicit ec: ExecutionContext,
      materializer: ActorMaterializer,
      config: AppConfig
  ) = {
    extractRequestEntity { requestEntity =>
      onSuccess( httpEntityToString( requestEntity, charset ) ) { requestBody =>
        headerValueByName( SlackEventSignatureVerifier.HttpHeaderNames.SIGNED_TIMESTAMP ) { signedTimestamp =>
          headerValueByName( SlackEventSignatureVerifier.HttpHeaderNames.SIGNED_HASH ).require(
            receivedHash =>
              signatureVerifier
                .verify( config.slackAppConfig.signingSecret, receivedHash, signedTimestamp, requestBody )
                .isRight,
            AuthorizationFailedRejection
          ) {
            route( requestBody )
          }
        }
      }
    }

  }

  def getLastSlackTokenFromDb[T]( teamId: String )(
      implicit timeout: Timeout = 3.seconds,
      slackTokensDb: ActorRef[SlackTokensDb.Command],
      context: ActorContext[T]
  ): Future[Option[SlackApiToken]] = {
    implicit val scheduler = context.system.scheduler
    implicit val ec: ExecutionContext = context.system.executionContext

    (slackTokensDb ? { ref: ActorRef[Option[SlackTokensDb.TeamTokensRecord]] =>
      SlackTokensDb.ReadTokens( teamId, ref )
    }).map( _.flatMap { record =>
      record.tokens.lastOption.flatMap { lastToken =>
        SlackApiToken.createFrom(
          tokenType = lastToken.tokenType,
          tokenValue = lastToken.tokenValue,
          scope = Some( lastToken.scope )
        )
      }
    } )
  }

  def routeWithSlackApiToken[T, B]( teamId: String )( route: SlackApiToken => Route )(
      implicit timeout: Timeout = 3.seconds,
      slackTokensDb: ActorRef[SlackTokensDb.Command],
      context: ActorContext[T]
  ): Route = {
    onSuccess(
      getLastSlackTokenFromDb( teamId )
    ) {
      case Some( apiToken ) => {
        route( apiToken )
      }
      case _ => {
        complete( StatusCodes.Unauthorized )
      }
    }
  }

}
