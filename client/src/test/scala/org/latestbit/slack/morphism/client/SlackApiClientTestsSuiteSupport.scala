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

package org.latestbit.slack.morphism.client

import java.util.Base64

import io.circe.syntax._
import io.circe._
import org.latestbit.slack.morphism.client.impl.SlackApiHttpProtocolSupport
import org.latestbit.slack.morphism.common._
import sttp.client3._
import sttp.model._

import scala.collection.immutable.Seq
import scala.concurrent.{ ExecutionContext, Future }

trait SlackApiClientTestsSuiteSupport {

  protected implicit val testApiUserToken =
    SlackApiUserToken( SlackAccessTokenValue( "test-token" ), Some( SlackApiTokenScope( "test-scope" ) ) )

  protected def createJsonResponseStub[RS](
      response: RS
  )( implicit encoder: Encoder.AsObject[RS], ec: ExecutionContext ) = {
    Future {
      Thread.sleep( 50 )
      Response(
        statusText = "OK",
        code = StatusCode.Ok,
        body = response.asJsonObject
          .add( "ok", true.asJson )
          .asJson
          .printWith( SlackApiHttpProtocolSupport.SlackJsonPrinter ),
        headers = Seq(
          Header.contentType( MediaType.ApplicationJson )
        )
      )
    }
  }

  protected def isExpectedJsonBody[RQ]( requestBody: RequestBody[_], expectedRequestBody: RQ )( implicit
      encoder: Encoder.AsObject[RQ]
  ): Boolean = {
    requestBody match {
      case StringBody( value, _, _ ) => {
        value == expectedRequestBody.asJsonObject.asJson.printWith( SlackApiHttpProtocolSupport.SlackJsonPrinter )
      }
      case _ => false
    }
  }

  protected def createTextResponseStub( text: String )( implicit ec: ExecutionContext ) = {
    Future {
      Thread.sleep( 50 )
      Response(
        statusText = "OK",
        code = StatusCode.Ok,
        body = text,
        headers = Seq(
          Header.contentType( MediaType.TextPlainUtf8 )
        )
      )
    }
  }

  protected def createBasicCredentials( clientId: String, clientSecret: String ) = {
    "Basic " ++
      new String(
        Base64.getEncoder.encode(
          s"$clientId:$clientSecret"
            .getBytes( SlackApiHttpProtocolSupport.SlackApiCharEncoding )
        ),
        SlackApiHttpProtocolSupport.SlackApiCharEncoding
      )
  }
}
