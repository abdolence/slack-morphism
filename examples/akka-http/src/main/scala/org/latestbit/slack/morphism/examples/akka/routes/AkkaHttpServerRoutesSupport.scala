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

import akka.http.scaladsl.model.{ HttpCharset, HttpCharsets, HttpEntity }
import akka.http.scaladsl.server.{ AuthorizationFailedRejection, Route }
import akka.http.scaladsl.server.Directives.{ complete, extractRequestEntity, headerValueByName, onSuccess }
import akka.stream.typed.scaladsl.ActorMaterializer
import akka.util.ByteString
import io.circe.Encoder
import io.circe.syntax._
import org.latestbit.slack.morphism.events.signature.SlackEventSignatureVerifier
import org.latestbit.slack.morphism.examples.akka.AppConfig

import scala.concurrent.{ ExecutionContext, Future }

trait AkkaHttpServerRoutesSupport {

  private final val signatureVerifier = new SlackEventSignatureVerifier()

  protected def httpEntityToString(
      entity: HttpEntity,
      charset: HttpCharset = HttpCharsets.`UTF-8`
  )( implicit ec: ExecutionContext, materializer: ActorMaterializer ): Future[String] = {
    entity.dataBytes.runFold( ByteString() )( _ ++ _ ).map { bs =>
      bs.decodeString( charset.value )
    }
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

}
