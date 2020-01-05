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

package org.latestbit.slack.morphism.client.templating

import java.time.Instant

/**
 * Slack mark down/field formatters
 */
trait SlackTextFormatters {

  /**
   * Format multi-line quoted text
   * @param srcText source text to quote
   * @return quoted text
   */
  protected def formatSlackQuoteText( srcText: String ) = {
    s">${srcText.replace( "\n", "\n>" )}"
  }

  /**
   * Format Slack Channel Id
   * @param channelId channel id
   * @return formatted channel id
   */
  protected def formatSlackChannelId( channelId: String ) = {
    s"<#${channelId}>"
  }

  /**
   * Format multiple Slack Channel Ids
   * @param ids channel ids
   * @return formatted channel ids
   */
  protected def formatSlackChannelIds( ids: Iterable[String] ) = {
    ids.map( formatSlackChannelId ).mkString( ", " )
  }

  /**
   * Format Slack User Id
   * @param userId user id
   * @return formatted user id
   */
  protected def formatSlackUserId( userId: String ) = {
    s"<@${userId}>"
  }

  /**
   * Format Slack Group Id
   * @param groupId group id
   * @return formatted group id
   */
  protected def formatSlackGroupId( groupId: String ) = {
    s"<!subteam^${groupId}>"
  }

  /**
   * Formate Slack Date/Time
   * https://api.slack.com/reference/surfaces/formatting#date-formatting
   * @param timestamp date time to format
   * @param formatType Slack Date/Time formatter ([[SlackTextFormatters.SlackDateFormatType]]
   * @param link an optional link on date/time
   * @return formatted Slack Date/Time
   */
  protected def formatDate(
      timestamp: Instant,
      formatType: SlackTextFormatters.SlackDateFormatType = SlackTextFormatters.SlackPrettyDateFormatType,
      link: Option[String] = None
  ): String = {
    val linkPart = link.map(value => s"^${value}" ).getOrElse( "" )
    s"<!date^${timestamp.getEpochSecond}^{${formatType.code}}${linkPart}|${timestamp.toString}>"
  }

}

object SlackTextFormatters {

  /**
   * Defines all available Slack Date/Time formatters
   * https://api.slack.com/reference/surfaces/formatting#date-formatting
   */
  sealed trait SlackDateFormatType { val code: String }

  case object SlackDateNumFormatType extends SlackDateFormatType { override val code: String = "date_num" }
  case object SlackCommonDateFormatType extends SlackDateFormatType { override val code: String = "date" }
  case object SlackShortDateFormatType extends SlackDateFormatType { override val code: String = "date_short" }
  case object SlackLongDateFormatType extends SlackDateFormatType { override val code: String = "date_long" }
  case object SlackPrettyDateFormatType extends SlackDateFormatType { override val code: String = "date_pretty" }

  case object SlackShortPrettyDateFormatType extends SlackDateFormatType {
    override val code: String = "date_short_pretty"
  }

  case object SlackLongPrettyDateFormatType extends SlackDateFormatType {
    override val code: String = "date_long_pretty"
  }
  case object SlackTimeFormatType extends SlackDateFormatType { override val code: String = "time" }
  case object SlackTimeSecsFormatType extends SlackDateFormatType { override val code: String = "time_secs" }
}
