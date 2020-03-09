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

import java.time.Instant
import java.util.Base64

import cats.data.EitherT
import io.circe.{ Encoder, JsonObject }
import io.circe.syntax._
import org.asynchttpclient.util.HttpConstants.Methods
import org.latestbit.slack.morphism.codecs.implicits._
import org.latestbit.slack.morphism.client.reqresp.channels.{
  SlackApiChannelsListRequest,
  SlackApiChannelsListResponse
}
import org.latestbit.slack.morphism.client.reqresp.chat.{
  SlackApiChatPostMessageRequest,
  SlackApiChatPostMessageResponse,
  SlackApiPostEventReply,
  SlackApiPostWebHookRequest
}
import org.latestbit.slack.morphism.client.reqresp.conversations.SlackApiConversationsHistoryRequest
import org.latestbit.slack.morphism.client.reqresp.test._
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.events.SlackUserMessage
import org.latestbit.slack.morphism.messages.SlackMessage
import org.scalatest.flatspec.AsyncFlatSpec
import sttp.client._
import sttp.client.testing.SttpBackendStub
import sttp.model._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.duration._

class CoreProtocolTestsSuite extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport {

  "A Slack client" should "able to connect and make a test api call" in {

    val mockResponse = SlackApiTestResponse( args = Map( "test" -> "test" ) )
    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture.whenAnyRequest
        .thenRespondWrapped(
          createJsonResponseStub( mockResponse )
        )
    val slackApiClient = new SlackApiClient()

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

    val slackApiClient = new SlackApiClient()

    slackApiClient.api.test( SlackApiTestRequest() ).map {
      case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
      case Left( ex: SlackApiResponseError ) => assert( ex.errorCode === "slack-test-error" )
      case Left( ex )                        => fail( ex )
    }
  }

  it should "able to make some basic API calls" in {
    import cats.implicits._

    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches( _.uri.path.contains( "channels.list" ) )
        .thenRespondWrapped(
          createJsonResponseStub(
            SlackApiChannelsListResponse(
              channels = List(
                SlackChannelInfo(
                  id = "channel-id",
                  name = "general",
                  flags = SlackChannelFlags(
                    is_general = Some( true )
                  ),
                  created = Instant.now()
                )
              )
            )
          )
        )
        .whenRequestMatches( _.uri.path.contains( "chat.postMessage" ) )
        .thenRespondWrapped(
          createJsonResponseStub(
            SlackApiChatPostMessageResponse(
              ts = "message-ts",
              message = SlackUserMessage(
                ts = "message-ts",
                user = "user-id"
              )
            )
          )
        )

    val slackApiClient = new SlackApiClient()

    SlackApiToken
      .createFrom(
        SlackApiToken.TokenTypes.BOT,
        "xoxb-89....."
      )
      .foreach { implicit slackApiToken: SlackApiToken =>
        EitherT( slackApiClient.channels.list( SlackApiChannelsListRequest() ) ).flatMap { channelsResp =>
          channelsResp.channels
            .find( _.flags.is_general.contains( true ) )
            .map { generalChannel =>
              EitherT(
                slackApiClient.chat
                  .postMessage(
                    SlackApiChatPostMessageRequest(
                      channel = generalChannel.id,
                      text = "Hello"
                    )
                  )
              ).map { resp => resp.ts.some }
            }
            .getOrElse(
              EitherT[Future, SlackApiClientError, Option[String]](
                Future.successful( None.asRight )
              )
            )
        }
      }

    SlackApiToken
      .createFrom(
        SlackApiToken.TokenTypes.BOT,
        "xoxb-89....."
      )
      .map { implicit slackApiToken: SlackApiToken =>
        slackApiClient.channels
          .list( SlackApiChannelsListRequest() )
          .flatMap {
            case Right( channelListResp ) => {
              channelListResp.channels
                .find( _.flags.is_general.contains( true ) )
                .map { generalChannel =>
                  slackApiClient.chat
                    .postMessage(
                      SlackApiChatPostMessageRequest(
                        channel = generalChannel.id,
                        text = "Hello"
                      )
                    )
                    .map( _.map( Option.apply ) )
                }
                .getOrElse(
                  Future.successful( Right( None ) )
                )
            }
            case Left( err ) => {
              Future.successful( Left( err ) )
            }
          }
          .map {
            case Right( Some( res ) ) => {
              assert( res.ts == "message-ts" )

            }
            case Right( _ )  => fail()
            case Left( err ) => fail( err )
          }
      }
      .getOrElse( fail( "No token" ) )

  }

  it should "able to post event replies using response_url without tokens" in {

    val testReply = SlackApiPostEventReply(
      text = "hey"
    )

    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches { req =>
          req.uri.path.contains( "some-response-url" ) &&
          req.method.method == Methods.POST &&
          createExpectedBody( req.body, testReply )
        }
        .thenRespondWrapped(
          createTextResponseStub( "Ok" )
        )

    val slackApiClient = new SlackApiClient()

    slackApiClient.chat
      .postEventReply(
        "https://example.net/some-response-url",
        testReply
      )
      .map {
        case Right( resp ) => assert( resp != null )
        case Left( ex )    => fail( ex )
      }
  }

  it should "able to post webhook messages using urls without tokens" in {

    val testWebHookMessage = SlackApiPostWebHookRequest(
      text = "hey"
    )

    implicit val testingBackend =
      SttpBackendStub.asynchronousFuture
        .whenRequestMatches { req =>
          req.uri.path.contains( "some-webhook-url" ) &&
          req.method.method == Methods.POST &&
          createExpectedBody( req.body, testWebHookMessage )
        }
        .thenRespondWrapped(
          createTextResponseStub( "Ok" )
        )

    val slackApiClient = new SlackApiClient()

    slackApiClient.chat
      .postWebhookMessage(
        "https://example.net/some-webhook-url",
        testWebHookMessage
      )
      .map {
        case Right( resp ) => assert( resp != null )
        case Left( ex )    => fail( ex )
      }
  }

}
