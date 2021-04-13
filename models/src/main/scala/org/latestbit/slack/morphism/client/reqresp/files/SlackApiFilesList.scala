package org.latestbit.slack.morphism.client.reqresp.files

import cats.data._
import org.latestbit.slack.morphism.client.streaming.SlackApiScrollableResponse
import org.latestbit.slack.morphism.common._

/**
 * Request of https://api.slack.com/methods/files.list
 */
case class SlackApiFilesListRequest(
    channel: Option[SlackChannelId] = None,
    count: Option[Long] = None,
    page: Option[Long],
    show_files_hidden_by_limit: Option[Boolean] = None,
    team_id: Option[SlackTeamId] = None,
    ts_from: Option[SlackTs] = None,
    ts_to: Option[SlackTs] = None,
    types: Option[NonEmptySet[SlackFileType]] = None,
    user: Option[SlackUserId] = None
)

/**
 * Response for https://api.slack.com/methods/files.list
 */
case class SlackApiFilesListResponse(
    files: List[SlackFileInfo],
    paging: Option[SlackApiFilesListResponsePaging] = None
) extends SlackApiScrollableResponse[SlackFileInfo, Long] {
  override val items: List[SlackFileInfo] = files
  override def getLatestPos: Option[Long] = paging.map( _.page ).filter( pageNum => paging.exists( _.pages < pageNum ) )

}

case class SlackApiFilesListResponsePaging( count: Long, total: Option[Long], page: Long, pages: Long )
