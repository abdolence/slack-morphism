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

import java.io.IOException

import io.circe
import org.latestbit.slack.morphism.common.SlackApiError
import sttp.model.{ StatusCode, Uri }

/**
 * Slack Web API error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param message error message
 * @param cause original cause
 */
sealed abstract class SlackApiClientError( uri: Uri, message: String, cause: Option[Throwable] = None )
    extends SlackApiError( message, cause )

/**
 * A trait to mark retryable errors
 */
trait SlackApiRetryableError

/**
 * System/unexpected Slack Web API error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param cause original cause
 */
case class SlackApiSystemError( uri: Uri, cause: Throwable )
    extends SlackApiClientError( uri = uri, message = cause.getMessage, cause = Some( cause ) )

/**
 * Slack Web API network/connection error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param cause original cause
 */
case class SlackApiConnectionError( uri: Uri, cause: IOException )
    extends SlackApiClientError( uri = uri, message = cause.getMessage, cause = Some( cause ) )
    with SlackApiRetryableError

/**
 * Slack Web API HTTP protocol error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param message error message
 * @param httpStatusCode HTTP status code
 * @param httpResponseBody HTTP response body
 */
case class SlackApiHttpError(
    uri: Uri,
    message: String,
    httpStatusCode: StatusCode,
    httpResponseBody: Option[String] = None
) extends SlackApiClientError( uri = uri, message )
    with SlackApiRetryableError

/**
 * Slack Web API protocol general error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param errorCode Slack error code
 * @param httpStatusCode HTTP status code
 * @param details error detail message
 * @param warning Slack warnings
 * @param messages Slack error messages
 */
case class SlackApiResponseError(
    uri: Uri,
    errorCode: String,
    httpStatusCode: StatusCode,
    details: Option[String] = None,
    warning: Option[String] = None,
    messages: Option[List[String]] = None
) extends SlackApiClientError(
      uri = uri,
      message = s"Slack API error response: ${errorCode}. Uri: ${uri}. " +
        s"${details.map( text => s" Details: ${text}.\n" ).getOrElse( "" )}" +
        s"${warning.map( text => s" Warning: ${text}.\n" ).getOrElse( "" )}" +
        s"${messages.map( msgs => s" Additional error messages: \n${msgs.mkString( "\n" )}" ).getOrElse( "" )}"
    )

/**
 * Slack Web API protocol rate limited error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param retryAfter retry after specified interval (in seconds)
 * @param details error detail message
 * @param warning Slack warnings
 * @param messages Slack error messages
 */
case class SlackApiRateLimitedError(
    uri: Uri,
    retryAfter: Option[Long] = None,
    details: Option[String] = None,
    warning: Option[String] = None,
    messages: Option[List[String]] = None
) extends SlackApiClientError(
      uri = uri,
      message = s"Slack API rate limited error. Uri: ${uri}." +
        s"${details.map( text => s" Details: ${text}.\n" ).getOrElse( "" )}" +
        s"${warning.map( text => s" Warning: ${text}.\n" ).getOrElse( "" )}" +
        s"${messages.map( msgs => s" Additional error messages: \n${msgs.mkString( "\n" )}" ).getOrElse( "" )}"
    )
    with SlackApiRetryableError

/**
 * Slack Web API JSON decoding error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param coderError JSON decoder error
 * @param httpResponseBody HTTP response body
 */
case class SlackApiDecodingError(
    uri: Uri,
    coderError: circe.Error,
    httpResponseBody: Option[String] = None
) extends SlackApiClientError(
      uri = uri,
      message = s"Json codec error: '${coderError.getMessage}'." +
        s"${httpResponseBody.map( text => s"\nReceived:\n${text}\n" ).getOrElse( "" )}",
      cause = Some( coderError )
    )

/**
 * Slack Wep API unexpected empty result has been received
 * @group ErrorDefs
 *
 * @param uri Web method URL
 */
case class SlackApiEmptyResultError( uri: Uri )
    extends SlackApiClientError(
      uri = uri,
      s"Expecting some result from ${uri.toString()}, but received nothing"
    )

/**
 * Slack Web API rate limit max delay error
 * @group ErrorDefs
 *
 * @param uri Web method URL
 * @param message error message
 */
case class SlackApiRateLimitMaxDelayError( uri: Uri, message: String ) extends SlackApiClientError( uri = uri, message )
