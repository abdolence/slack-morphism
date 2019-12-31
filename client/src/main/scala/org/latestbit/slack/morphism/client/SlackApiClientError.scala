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
import sttp.model.Uri

sealed abstract class SlackApiClientError( uri: Uri, message: String, cause: Option[Throwable] = None )
    extends SlackApiError( message, cause )

case class SlackApiSystemError( uri: Uri, cause: Throwable )
    extends SlackApiClientError( uri = uri, message = cause.getMessage, cause = Some( cause ) )

case class SlackApiConnectionError( uri: Uri, cause: IOException )
    extends SlackApiClientError( uri = uri, message = cause.getMessage, cause = Some( cause ) )

case class SlackApiHttpError( uri: Uri, details: String, httpResponseBody: Option[String] = None )
    extends SlackApiClientError( uri = uri, details )

case class SlackApiResponseError(
    uri: Uri,
    errorCode: String,
    details: Option[String] = None,
    warning: Option[String] = None
) extends SlackApiClientError(
      uri = uri,
      message = s"""Slack API error response: ${errorCode}.
			   |${details.map(text => s" Details: ${text}." ).getOrElse( "" )}
			   |${warning.map(text => s" Warning: ${text}." ).getOrElse( "" )}
			   |""".stripMargin
    )

case class SlackApiDecodingError(
    uri: Uri,
    coderError: circe.Error,
    httpResponseBody: Option[String] = None
) extends SlackApiClientError( uri = uri, message = coderError.getMessage, cause = Some( coderError ) )

case class SlackApiEmptyResultError( uri: Uri )
    extends SlackApiClientError(
      uri = uri,
      s"Expecting some result from ${uri.toString()}, but received nothing"
    )
