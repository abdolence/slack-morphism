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
   * Format Slack Date/Time
   * https://api.slack.com/reference/surfaces/formatting#date-formatting
   * @param timestamp date time to format
   * @param token_string provide a formatting for your timestamp, using plain text along with special Slack tokens
   * @param link an optional link on date/time
   * @return formatted Slack Date/Time
   */
  protected def formatDate(
      timestamp: Instant,
      token_string: String = SlackTextFormatters.SlackDateTimeFormats.DEFAULT,
      link: Option[String] = None
  ): String = {
    val linkPart = link.map( value => s"^${value}" ).getOrElse( "" )
    s"<!date^${timestamp.getEpochSecond}^${token_string}${linkPart}|${timestamp.toString}>"
  }

  /**
   * Format a Slack URL on some text
   * @param url URL
   * @param text text
   * @return formatted url
   */
  protected def formatUrl( url: String, text: String ) = {
    s"<${url}|${text}>"
  }

}

/**
 * Slack mark down/field formatters
 */
object SlackTextFormatters {

  /**
   * Defines default Slack Date/Time formats from:
   * https://api.slack.com/reference/surfaces/formatting#date-formatting
   */
  object SlackDateTimeFormats {
    final val DATE_NUM = "{date_num}"
    final val DATE = "{date}"
    final val DATE_SHORT = "{date_short}"
    final val DATE_LONG = "{date_long}"
    final val DATE_PRETTY = "{date_pretty}"
    final val DATE_SHORT_PRETTY = "{date_short_pretty}"
    final val DATE_LONG_PRETTY = "{date_long_pretty}"
    final val TIME = "{time}"
    final val TIME_SECS = "{time_secs}"

    final val DEFAULT = DATE_PRETTY
  }
}
