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

package org.latestbit.slack.morphism.client.impl

import java.io.IOException

import cats.implicits._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.reqresp.internal.SlackGeneralResponseParams
import sttp.client._
import sttp.model.{ MediaType, Uri }

import scala.concurrent.{ ExecutionContext, Future }

trait SlackApiHttpProtocolSupport {

  import SlackApiHttpProtocolSupport._

  type SttpFutureBackend = SttpBackend[Future, Nothing, NothingT]

  protected type SlackApiEmptyType = JsonObject
  protected val SLACK_EMPTY_REQUEST: SlackApiEmptyType = JsonObject()

  protected def checkIfContentTypeIsJson( contentType: MediaType ) = {
    contentType.mainType == MediaType.ApplicationJson.mainType &&
    contentType.subType == MediaType.ApplicationJson.subType
  }

  protected def slackGeneralResponseToError(
      uri: Uri,
      generalResponseParams: SlackGeneralResponseParams
  ): Option[SlackApiClientError] = {
    generalResponseParams.error.map { errorCode =>
      SlackApiResponseError(
        uri = uri,
        errorCode = errorCode,
        warning = generalResponseParams.warning
      )
    }
  }

  protected def circeDecodingErrorToApiError(
      uri: Uri,
      error: io.circe.Error,
      body: String
  ): SlackApiClientError = {
    SlackApiDecodingError(
      uri = uri,
      coderError = error,
      httpResponseBody = Option( body )
    )
  }

  protected def decodeSlackGeneralResponse( body: String ) = {
    decode[SlackGeneralResponseParams]( body )
  }

  protected def decodeSlackResponse[RS]( uri: Uri, response: Response[Either[String, String]] )(
      implicit decoder: Decoder[RS]
  ): Either[SlackApiClientError, RS] = {
    val responseMediaType =
      response.contentType.map( MediaType.parse ).flatMap( _.toOption )

    response.body match {
      case Right( successBody ) if responseMediaType.exists( checkIfContentTypeIsJson ) => {
        decodeSlackGeneralResponse( successBody ) match {
          case Right( generalResp ) => {
            slackGeneralResponseToError( uri, generalResp )
              .map( Either.left[SlackApiClientError, RS] )
              .getOrElse(
                decode[RS]( successBody ).left
                  .map(ex => circeDecodingErrorToApiError( uri, ex, successBody ) )
              )
          }
          case Left( ex ) => {
            Left( circeDecodingErrorToApiError( uri, ex, successBody ) )
          }
        }
      }
      case Right( _ ) => {
        Left( SlackApiEmptyResultError( uri = uri ) )
      }
      case Left( errorBody ) if responseMediaType.exists( checkIfContentTypeIsJson ) && errorBody.nonEmpty => {
        Left(
          decodeSlackGeneralResponse( errorBody ) match {
            case Right( generalResp ) =>
              slackGeneralResponseToError( uri, generalResp ).getOrElse(
                SlackApiHttpError(
                  uri = uri,
                  details = s"HTTP error / ${response.code}: ${response.statusText}.\n${errorBody}",
                  httpResponseBody = Option( errorBody )
                )
              )
            case Left( ex ) => {
              circeDecodingErrorToApiError( uri, ex, errorBody )
            }
          }
        )
      }
      case Left( errorBody ) => {
        Left(
          SlackApiHttpError(
            uri = uri,
            details = s"HTTP error / ${response.code}: ${response.statusText}.\n${Option( errorBody )
              .map { body =>
                s": ${body}"
              }
              .getOrElse( "" )}",
            httpResponseBody = Option( errorBody )
          )
        )
      }
    }
  }

  protected def sendSlackRequest[RS]( request: Request[Either[String, String], Nothing] )(
      implicit decoder: Decoder[RS],
      backend: SttpFutureBackend,
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {
    request.send().map(response => decodeSlackResponse[RS]( request.uri, response ) ).recoverWith {
      case ex: IOException =>
        Future.successful( Left( SlackApiConnectionError( request.uri, ex ) ) )
      case ex: Throwable =>
        Future.successful( Left( SlackApiSystemError( request.uri, ex ) ) )
    }
  }

  protected def protectedSlackHttpApiRequest[RS](
      request: Request[Either[String, String], Nothing]
  )(
      implicit slackApiToken: SlackApiToken,
      decoder: Decoder[RS],
      backend: SttpFutureBackend,
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {
    sendSlackRequest[RS](
      request.auth.bearer( slackApiToken.value )
    )
  }

  protected def createSlackHttpApiRequest(): RequestT[Empty, Either[String, String], Nothing] = {
    basicRequest
  }

  protected def getSlackMethodAbsoluteUri( methodUri: String ): Uri =
    uri"${SLACK_BASE_URI}/${methodUri}"

  protected def protectedSlackHttpApiPost[RQ, RS]( methodUri: String, body: RQ )(
      implicit slackApiToken: SlackApiToken,
      encoder: Encoder[RQ],
      decoder: Decoder[RS],
      backend: SttpFutureBackend,
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {
    val bodyAsStr = body.asJson.dropNullValues.noSpaces

    protectedSlackHttpApiRequest[RS](
      createSlackHttpApiRequest()
        .body(
          StringBody(
            bodyAsStr,
            SLACK_API_CHAR_ENCODING,
            Some( MediaType.ApplicationJson )
          )
        )
        .post( getSlackMethodAbsoluteUri( methodUri ) )
    )
  }

  protected def protectedSlackHttpApiGet[RS](
      methodUri: String,
      request: RequestT[Empty, Either[String, String], Nothing],
      params: Map[String, Option[String]] = Map()
  )(
      implicit slackApiToken: SlackApiToken,
      decoder: Decoder[RS],
      backend: SttpFutureBackend,
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {

    val filteredParams: Map[String, String] =
      params.foldLeft( Map[String, String]() ) {
        case ( acc, ( k, v ) ) =>
          v.map( acc.updated( k, _ ) ).getOrElse( acc )
      }
    protectedSlackHttpApiRequest[RS](
      request.get( getSlackMethodAbsoluteUri( methodUri ).params( filteredParams ) )
    )
  }

  object http {

    /**
     * Make HTTP GET to Slack API
     * @param methodUri a relative method uri (like 'api.test')
     * @param params HTTP GET URL params
     * @return Decoded from JSON result
     */
    def get[RS]( methodUri: String, params: Map[String, Option[String]] = Map() )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        decoder: Decoder[RS],
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, RS]] = {
      protectedSlackHttpApiGet( methodUri, createSlackHttpApiRequest() )
    }

    /**
     * Make HTTP GET to Slack API
     * @param methodUri a relative method uri (like 'api.test')
     * @param req a request model to encode to JSON
     * @return Decoded from JSON result
     */
    def post[RQ, RS]( methodUri: String, req: RQ )(
        implicit slackApiToken: SlackApiToken,
        backend: SttpFutureBackend,
        encoder: Encoder[RQ],
        decoder: Decoder[RS],
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, RS]] = {
      protectedSlackHttpApiPost[RQ, RS](
        methodUri,
        req
      )
    }

  }

}

object SlackApiHttpProtocolSupport {
  val SLACK_BASE_URI = "https://slack.com/api"
  val SLACK_API_CHAR_ENCODING = "UTF-8"
}
