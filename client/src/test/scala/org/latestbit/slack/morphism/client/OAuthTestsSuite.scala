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

import org.latestbit.slack.morphism.client.reqresp.oauth._
import org.latestbit.slack.morphism.common._
import org.scalatest.flatspec.AsyncFlatSpec
import sttp.client3.testing.SttpBackendStub
import sttp.model.HeaderNames
import cats.instances.future._
import org.latestbit.slack.morphism.codecs.CirceCodecs

class OAuthTestsSuite extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport with CirceCodecs {

  it should "get Slack OAuth v2 access codes" in {

    val mockResponse = SlackOAuthV2AccessTokenResponse(
      access_token = SlackAccessTokenValue( "access-token-value" ),
      token_type = SlackApiTokenType.Bot,
      scope = SlackApiTokenScope( "something:something,anything:anything" ),
      team = SlackTeamInfo(
        id = SlackTeamId( "test-slack-workspace-id" ),
        name = Some( "test-slack-workspace-name" )
      ),
      app_id = SlackAppId( "test-app-id" ),
      authed_user = SlackOAuthV2AuthedUser(
        id = SlackUserId( "test-auth-user-id" )
      )
    )
    val mockClientId     = "test-client-id"
    val mockClientSecret = "test-client-secret"

    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches { req =>
          req.headers.exists( header =>
            header.is( HeaderNames.Authorization ) &&
              header.value == createBasicCredentials( mockClientId, mockClientSecret )
          )
        }
        .thenRespondF(
          createJsonResponseStub( mockResponse )
        )

    val slackApiClient = SlackApiClient.create()

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
