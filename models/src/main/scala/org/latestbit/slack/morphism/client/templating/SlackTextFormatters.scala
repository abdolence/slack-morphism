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

import org.latestbit.slack.morphism.common.{ SlackChannelId, SlackUserId }

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
  protected def formatSlackChannelId( channelId: SlackChannelId ) = {
    s"<#${channelId.value}>"
  }

  /**
   * Format multiple Slack Channel Ids
   * @param ids channel ids
   * @return formatted channel ids
   */
  protected def formatSlackChannelIds( ids: Iterable[SlackChannelId] ) = {
    ids.map( formatSlackChannelId ).mkString( ", " )
  }

  /**
   * Format Slack User Id
   * @param userId user id
   * @return formatted user id
   */
  protected def formatSlackUserId( userId: SlackUserId ) = {
    s"<@${userId.value}>"
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
      token_string: String = SlackTextFormatters.SlackDateTimeFormats.Default,
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
    final val DateNum = "{date_num}"
    final val Date = "{date}"
    final val DateShort = "{date_short}"
    final val DateLong = "{date_long}"
    final val DatePretty = "{date_pretty}"
    final val DateShortPretty = "{date_short_pretty}"
    final val DateLongPretty = "{date_long_pretty}"
    final val Time = "{time}"
    final val TimeSecs = "{time_secs}"

    final val Default = DatePretty
  }
}
