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

package org.latestbit.slack.morphism.examples.akka

import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.stream.typed.scaladsl.ActorMaterializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.latestbit.slack.morphism.events._
import org.latestbit.slack.morphism.events.signature.SlackEventSignatureVerifier

import io.circe._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.{ ExecutionContext, Future }

class AkkaHttpServerRoutes( implicit ctx: ActorContext[_], materializer: ActorMaterializer, config: AppConfig )
    extends LazyLogging {
  implicit val ec: ExecutionContext = ctx.system.executionContext
  private final val signatureVerifier = new SlackEventSignatureVerifier()

  protected def httpEntityToString(
      entity: HttpEntity,
      charset: HttpCharset = HttpCharsets.`UTF-8`
  ): Future[String] = {
    entity.dataBytes.runFold( ByteString() )( _ ++ _ ).map { bs =>
      bs.decodeString( charset.value )
    }
  }

  def extractSlackSignedEvent( route: SlackPushEvent => Route, charset: HttpCharset = HttpCharsets.`UTF-8` ) = {
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
            decode[SlackPushEvent]( requestBody ) match {
              case Right( event ) => route( event )
              case Left( ex ) => {
                logger.error( "Can't decode push event from Slack.", ex )
                complete( StatusCodes.BadRequest )
              }
            }
          }
        }
      }
    }

  }

  def completeWithJson[T]( response: T )( implicit encoder: Encoder.AsObject[T] ) = {
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    complete(
      response.asJson
    )
  }

  def createRoutes(): Route = {
    ignoreTrailingSlash {
      path( "test" ) {
        get {
          complete( StatusCodes.OK )
        }
      } ~
        path( "push" ) {
          post {
            extractSlackSignedEvent {
              case ev: SlackUrlVerificationEvent => {
                logger.info( s"Received a challenge request:\n${ev.challenge}" )
                complete(
                  StatusCodes.OK,
                  HttpEntity(
                    ContentTypes.`text/plain(UTF-8)`,
                    ev.challenge
                  )
                )
              }
              case ev: SlackPushEvent => {
                logger.warn( s"Unsupported event received: ${ev}" )
                complete( StatusCodes.OK )
              }
            }
          }
        }
    }
  }
}
