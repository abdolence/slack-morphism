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

package org.latestbit.slack.morphism.client.reqresp.auth

import org.latestbit.slack.morphism.client.streaming.SlackApiScrollableResponse
import org.latestbit.slack.morphism.common.{ SlackApiResponseMetadata, SlackBasicTeamInfo, SlackCursorId }

/**
 * Request of https://api.slack.com/methods/auth.teams.list
 */
case class SlackApiAuthTeamListRequest(
    include_icon: Option[Boolean] = None,
    cursor: Option[SlackCursorId] = None,
    limit: Option[Long] = None
)

/**
 * Response of https://api.slack.com/methods/auth.teams.list
 */
case class SlackApiAuthTeamListResponse(
    teams: List[SlackBasicTeamInfo],
    response_metadata: Option[SlackApiResponseMetadata] = None
) extends SlackApiScrollableResponse[SlackBasicTeamInfo, SlackCursorId] {

  override def items: List[SlackBasicTeamInfo]     = teams
  override def getLatestPos: Option[SlackCursorId] = response_metadata.flatMap( _.next_cursor )

}
