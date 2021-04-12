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

import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.client.reqresp.files._
import org.latestbit.slack.morphism.codecs.implicits._
import sttp.client._

import cats.data._
import cats.implicits._

import java.io.{ FileInputStream, InputStream }

/**
 * Support for Slack Files API methods
 */
trait SlackApiFilesClient[F[_]] extends SlackApiHttpProtocolSupport[F] {

  object files {

    /**
     * https://api.slack.com/methods/files.upload
     */
    def upload( req: SlackApiFilesUploadRequest, fileInputStream: InputStream )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiFilesUploadResponse]] = {

      sendManagedSlackHttpRequest[SlackApiFilesUploadResponse](
        createSlackHttpApiRequest()
          .post(
            getSlackMethodAbsoluteUri( "files.upload" )
          )
          .multipartBody(
            Seq(
              Some( multipart( "file", fileInputStream ).fileName( req.filename ) ),
              req.channels.map( channels => multipart( "channels", channels.map( _.value ).intercalate( "," ) ) ),
              Some( multipart( "filename", req.filename ) ),
              req.filetype.map( filetype => multipart( "filetype", filetype.value ) ),
              req.initial_comment.map( initial_comment => multipart( "initial_comment", initial_comment ) ),
              req.thread_ts.map( thread_ts => multipart( "thread_ts", thread_ts.value ) ),
              req.title.map( title => multipart( "title", title ) )
            ).flatten
          ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) ),
        slackApiToken = Some( slackApiToken )
      )
    }
  }

}
