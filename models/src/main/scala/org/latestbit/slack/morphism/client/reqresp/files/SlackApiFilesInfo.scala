package org.latestbit.slack.morphism.client.reqresp.files

import org.latestbit.slack.morphism.common._

/**
 * Request of https://api.slack.com/methods/files.info
 */
case class SlackApiFilesInfoRequest(
    file: SlackFileId
)

/**
 * Response for https://api.slack.com/methods/files.info
 */
case class SlackApiFilesInfoResponse(
    file: Option[SlackFileInfo]
)
