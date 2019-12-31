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

import io.circe.{ Encoder, JsonObject }
import io.circe.generic.auto._
import io.circe.syntax._
import org.latestbit.slack.morphism.client.reqresp.test._
import org.scalatest.flatspec.AsyncFlatSpec
import sttp.client._
import sttp.client.testing.SttpBackendStub
import sttp.model.{ Header, HeaderNames, Headers, MediaType, StatusCode }

import scala.collection.immutable.Seq
import scala.concurrent.Future

class CoreProtocolTestsSuite extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport {

  "A Slack client" should "able to connect and make a test api call" in {

    val mockResponse = SlackApiTestResponse( args = Map( "test" -> "test" ) )
    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture.whenAnyRequest
        .thenRespondWrapped(
          createResponseStub( mockResponse )
        )

    slackApiClient.api.test( SlackApiTestRequest() ).map {
      case Right( resp ) => assert( mockResponse === resp )
      case Left( ex )    => fail( ex )
    }
  }

  it should "detect Slack API error responses" in {
    implicit val testingBackend = SttpBackendStub.asynchronousFuture.whenAnyRequest
      .thenRespondWrapped(
        Future {
          Response(
            statusText = "OK",
            code = StatusCode.Ok,
            body = JsonObject(
              "ok" -> false.asJson,
              "error" -> "slack-test-error".asJson
            ).asJson.dropNullValues.noSpaces,
            headers = Seq(
              Header.contentType( MediaType.ApplicationJson )
            ),
            history = Nil
          )
        }
      )

    slackApiClient.api.test( SlackApiTestRequest() ).map {
      case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
      case Left( ex: SlackApiResponseError ) => assert( ex.errorCode === "slack-test-error" )
      case Left( ex )                        => fail( ex )
    }
  }

}
