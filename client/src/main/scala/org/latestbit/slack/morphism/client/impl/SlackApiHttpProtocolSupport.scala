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
import io.circe.parser._
import io.circe.syntax._
import org.latestbit.slack.morphism.codecs.implicits._

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.reqresp.internal.SlackGeneralResponseParams
import sttp.client._
import sttp.model.{ MediaType, Uri }

import scala.concurrent.{ ExecutionContext, Future }

trait SlackApiHttpProtocolSupport extends SlackApiClientBackend {

  import SlackApiHttpProtocolSupport._

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
        warning = generalResponseParams.warning,
        messages = generalResponseParams.response_metadata.flatMap( _.messages )
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
                  .map( ex => circeDecodingErrorToApiError( uri, ex, successBody ) )
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
                  message = s"HTTP error / ${response.code}: ${response.statusText}.\n${errorBody}",
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
            message = s"HTTP error / ${response.code}: ${response.statusText}.\n${Option( errorBody )
              .map { body => s": ${body}" }
              .getOrElse( "" )}",
            httpResponseBody = Option( errorBody )
          )
        )
      }
    }
  }

  protected def sendSlackRequest[RS]( request: Request[Either[String, String], Nothing] )(
      implicit decoder: Decoder[RS],
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {
    request.send().map( response => decodeSlackResponse[RS]( request.uri, response ) ).recoverWith {
      case ex: IOException =>
        Future.successful( Left( SlackApiConnectionError( request.uri, ex ) ) )
      case ex: Throwable =>
        Future.successful( Left( SlackApiSystemError( request.uri, ex ) ) )
    }
  }

  protected def protectedSlackHttpApiRequest[RS](
      request: Request[Either[String, String], Nothing],
      methodTierLevel: Option[Int]
  )(
      implicit slackApiToken: SlackApiToken,
      decoder: Decoder[RS],
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

  protected def encodePostBody[RQ](
      request: RequestT[Empty, Either[String, String], Nothing],
      body: RQ
  )( implicit encoder: Encoder[RQ] ): RequestT[Empty, Either[String, String], Nothing] = {
    val bodyAsStr = body.asJson.printWith( SlackJsonPrinter )
    request
      .body(
        StringBody(
          bodyAsStr,
          SLACK_API_CHAR_ENCODING,
          Some( MediaType.ApplicationJson.charset( SLACK_API_CHAR_ENCODING ) )
        )
      )
  }

  protected def protectedSlackHttpApiPost[RQ, RS](
      absoluteUri: Uri,
      request: RequestT[Empty, Either[String, String], Nothing],
      body: RQ,
      methodTierLevel: Option[Int]
  )(
      implicit slackApiToken: SlackApiToken,
      encoder: Encoder[RQ],
      decoder: Decoder[RS],
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {

    protectedSlackHttpApiRequest[RS](
      encodePostBody[RQ]( request, body )
        .post( absoluteUri ),
      methodTierLevel
    )

  }

  protected def protectedSlackHttpApiPost[RQ, RS]( methodUri: String, body: RQ, methodTierLevel: Option[Int] )(
      implicit slackApiToken: SlackApiToken,
      encoder: Encoder[RQ],
      decoder: Decoder[RS],
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {
    protectedSlackHttpApiPost[RQ, RS](
      absoluteUri = getSlackMethodAbsoluteUri( methodUri ),
      request = createSlackHttpApiRequest(),
      body = body,
      methodTierLevel = methodTierLevel
    )
  }

  protected def protectedSlackHttpApiGet[RS](
      methodUri: String,
      request: RequestT[Empty, Either[String, String], Nothing],
      params: Map[String, Option[String]],
      methodTierLevel: Option[Int]
  )(
      implicit slackApiToken: SlackApiToken,
      decoder: Decoder[RS],
      ec: ExecutionContext
  ): Future[Either[SlackApiClientError, RS]] = {

    val filteredParams: Map[String, String] =
      params.foldLeft( Map[String, String]() ) {
        case ( acc, ( k, v ) ) =>
          v.map( acc.updated( k, _ ) ).getOrElse( acc )
      }
    protectedSlackHttpApiRequest[RS](
      request.get( getSlackMethodAbsoluteUri( methodUri ).params( filteredParams ) ),
      methodTierLevel
    )
  }

  /**
   * Some of Slack responses historically returns HTTP plain text responses with 'Ok' body, instead JSON.
   * So, this auxiliary function helps to fix and hide this behaviour.
   *
   * @note There are very few methods that behave like that,
   *       so we're fixing it for those particular functions,
   *       instead of generalising this behaviour for other API methods.
   *
   * @param replacement what should be returned instead HTTP OK
   * @param either a response result to fix
   * @tparam RS response type
   * @return either other error or fixed empty result
   */
  protected def handleSlackEmptyRes[RS]( replacement: => RS )( either: Either[SlackApiClientError, RS] ) = {
    either.leftFlatMap {
      case _: SlackApiEmptyResultError => {
        Right( replacement )
      }
      case err: SlackApiClientError => err.asLeft
    }
  }

  object http {

    /**
     * Make HTTP GET to Slack API
     * @param methodUri a relative method uri (like 'api.test')
     * @param params HTTP GET URL params
     * @tparam RS expected response type
     * @return Decoded from JSON result
     */
    def get[RS]( methodUri: String, params: Map[String, Option[String]] = Map(), methodTierLevel: Option[Int] = None )(
        implicit slackApiToken: SlackApiToken,
        decoder: Decoder[RS],
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, RS]] = {
      protectedSlackHttpApiGet[RS]( methodUri, createSlackHttpApiRequest(), params, methodTierLevel )
    }

    /**
     * Make HTTP POST to Slack API
     * @param methodUri a relative method uri (like 'api.test')
     * @param req a request model to encode to JSON
     * @tparam RQ request type
     * @tparam RS expected response type
     * @return Decoded from JSON result
     */
    def post[RQ, RS]( methodUri: String, req: RQ, methodTierLevel: Option[Int] = None )(
        implicit slackApiToken: SlackApiToken,
        encoder: Encoder[RQ],
        decoder: Decoder[RS],
        ec: ExecutionContext
    ): Future[Either[SlackApiClientError, RS]] = {
      protectedSlackHttpApiPost[RQ, RS](
        methodUri,
        req,
        methodTierLevel
      )
    }

  }

}

object SlackApiHttpProtocolSupport {
  val SLACK_BASE_URI = "https://slack.com/api"
  val SLACK_API_CHAR_ENCODING = "UTF-8"

  final val SlackJsonPrinter = Printer(
    dropNullValues = true,
    indent = ""
  )
}
