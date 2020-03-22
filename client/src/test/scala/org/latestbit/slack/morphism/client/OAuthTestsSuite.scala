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

import io.circe.generic.auto._
import org.latestbit.slack.morphism.client.reqresp.oauth._
import org.latestbit.slack.morphism.common.SlackTeamInfo
import org.scalatest.flatspec.AsyncFlatSpec
import sttp.client.testing.SttpBackendStub
import sttp.model.HeaderNames

import scala.concurrent.Future
import cats.instances.future._

class OAuthTestsSuite extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport {

  it should "get Slack OAuth v1 access codes" in {

    val mockResponse = SlackOAuthV1AccessTokenResponse(
      access_token = "access-token-value",
      scope = "something:something,anything:anything",
      team_id = "test-team-id"
    )
    val mockClientId = "test-client-id"
    val mockClientSecret = "test-client-secret"

    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches { req =>
          req.headers.exists( header =>
            header.is( HeaderNames.Authorization ) &&
              header.value == createBasicCredentials( mockClientId, mockClientSecret )
          )
        }
        .thenRespondWrapped(
          createJsonResponseStub( mockResponse )
        )
    val slackApiClient = new SlackApiClient[Future]()

    slackApiClient.oauth
      .access(
        clientId = mockClientId,
        clientSecret = mockClientSecret,
        code = "test-code"
      )
      .map {
        case Right( resp ) => assert( mockResponse === resp )
        case Left( ex )    => fail( ex )
      }
  }

  it should "get Slack OAuth v2 access codes" in {

    val mockResponse = SlackOAuthV2AccessTokenResponse(
      access_token = "access-token-value",
      token_type = "test-token-type",
      scope = "something:something,anything:anything",
      team = SlackTeamInfo(
        id = "test-slack-workspace-id",
        name = Some( "test-slack-workspace-name" )
      ),
      app_id = "test-app-id",
      authed_user = SlackOAuthV2AuthedUser(
        id = "test-auth-user-id"
      )
    )
    val mockClientId = "test-client-id"
    val mockClientSecret = "test-client-secret"

    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches { req =>
          req.headers.exists( header =>
            header.is( HeaderNames.Authorization ) &&
              header.value == createBasicCredentials( mockClientId, mockClientSecret )
          )
        }
        .thenRespondWrapped(
          createJsonResponseStub( mockResponse )
        )

    val slackApiClient = new SlackApiClient()

    slackApiClient.oauth.v2
      .access(
        clientId = mockClientId,
        clientSecret = mockClientSecret,
        code = "test-code"
      )
      .map {
        case Right( resp ) => assert( mockResponse === resp )
        case Left( ex )    => fail( ex )
      }
  }

}
