/*
 * Copyright 2021 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

import cats.implicits._
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import org.latestbit.slack.morphism.client.reqresp.files._
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.latestbit.slack.morphism.codecs.implicits._
import org.latestbit.slack.morphism.common._
import sttp.client3._
import sttp.model.Part

import java.io.InputStream

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

      val parts: Seq[Part[BasicRequestBody]] = Seq(
        req.channels.map( channels => multipart( "channels", channels.map( _.value ).intercalate( "," ) ) ),
        Some( multipart( "filename", req.filename ) ),
        req.filetype.map( filetype => multipart( "filetype", filetype.value ) ),
        req.initial_comment.map( initial_comment => multipart( "initial_comment", initial_comment ) ),
        req.thread_ts.map( thread_ts => multipart( "thread_ts", thread_ts.value ) ),
        req.title.map( title => multipart( "title", title ) )
      ).flatten

      sendManagedSlackHttpRequest[SlackApiFilesUploadResponse](
        createSlackHttpApiRequest()
          .post(
            getSlackMethodAbsoluteUri( "files.upload" )
          )
          .multipartBody(
            multipart( "file", fileInputStream ).fileName( req.filename ),
            parts: _*
          ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier2 ) ) ),
        slackApiToken = Some( slackApiToken )
      )
    }

    /**
     * https://api.slack.com/methods/files.list
     */
    def list( req: SlackApiFilesListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiFilesListResponse]] = {

      http.get[SlackApiFilesListResponse](
        "files.list",
        Map(
          "page"                       -> req.page.map( _.toString() ),
          "count"                      -> req.count.map( _.toString() ),
          "ts_to"                      -> req.ts_to.map( _.toString() ),
          "ts_from"                    -> req.ts_from.map( _.toString() ),
          "show_files_hidden_by_limit" -> req.show_files_hidden_by_limit.map( _.toString() ),
          "team_id"                    -> req.team_id.map( _.value ),
          "channel"                    -> req.channel.map( _.value ),
          "types"                      -> req.types.map( _.map( _.value ).intercalate( "," ) ),
          "user"                       -> req.user.map( _.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

    /**
     * Scrolling support for
     * https://api.slack.com/methods/files.list
     */
    def listScroller( req: SlackApiFilesListRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): SlackApiResponseScroller[F, SlackFileInfo, Long, SlackApiFilesListResponse] = {
      new SlackApiResponseScroller[F, SlackFileInfo, Long, SlackApiFilesListResponse](
        initialLoader = { () => list( req ) },
        batchLoader = { lastPage =>
          list(
            SlackApiFilesListRequest(
              page = Some( lastPage + 1 ),
              count = req.count
            )
          )
        }
      )
    }

    /**
     * https://api.slack.com/methods/files.info
     */
    def info( req: SlackApiFilesInfoRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiFilesInfoResponse]] = {

      http.get[SlackApiFilesInfoResponse](
        "files.info",
        Map(
          "file" -> Some( req.file.value )
        ),
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier4 ) ) )
      )
    }

    /**
     * https://api.slack.com/methods/files.delete
     */
    def delete( req: SlackApiFilesDeleteRequest )( implicit
        slackApiToken: SlackApiToken,
        backendType: SlackApiClientBackend.BackendType[F]
    ): F[Either[SlackApiClientError, SlackApiFilesDeleteResponse]] = {

      http.post[SlackApiFilesDeleteRequest, SlackApiFilesDeleteResponse](
        "files.delete",
        req,
        methodRateControl = Some( SlackApiMethodRateControlParams( tier = Some( SlackApiRateControlParams.Tier3 ) ) )
      )
    }

  }

}
