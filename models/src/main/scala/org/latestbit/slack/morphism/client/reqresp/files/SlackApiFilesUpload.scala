package org.latestbit.slack.morphism.client.reqresp.files

import cats.data.NonEmptySet
import org.latestbit.slack.morphism.common._

/**
 * Request of https://api.slack.com/methods/files.upload
 */

case class SlackApiFilesUploadRequest(
    channels: Option[NonEmptySet[SlackChannelId]] = None,
    filename: String,
    filetype: Option[SlackFileType] = None,
    initial_comment: Option[String] = None,
    thread_ts: Option[SlackTs] = None,
    title: Option[String] = None
)

/**
 * Request of https://api.slack.com/methods/files.upload
 */
case class SlackApiFilesUploadResponse( file: SlackFileInfo )
