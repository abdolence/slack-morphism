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

package org.latestbit.slack.morphism.client.models.common

import java.time.Instant
import java.util.Date

import io.circe.{ Decoder, Encoder, HCursor, Json }

import scala.language.implicitConversions

case class SlackDateTime( value: Instant ) extends AnyVal

object SlackDateTime {

  implicit def toSlackDateTime( value: Instant ): SlackDateTime =
    SlackDateTime( value )

  implicit def toSlackDateTime( value: Date ): SlackDateTime =
    toSlackDateTime( value.toInstant )

  implicit def toSlackDateTime( value: Long ): SlackDateTime =
    toSlackDateTime( Instant.ofEpochSecond( value ) )

  implicit def fromSlackDateTimeToInstant( dateTime: SlackDateTime ): Instant =
    dateTime.value

  implicit def fromSlackDateTimeToDate( dateTime: SlackDateTime ): Date =
    Date.from( dateTime.value )

  implicit def fromSlackDateTimeToLong( dateTime: SlackDateTime ): Long =
    dateTime.value.getEpochSecond

  implicit val slackDateTimeEncoder: Encoder[SlackDateTime] = (a: SlackDateTime) => {
    Json.fromLong( fromSlackDateTimeToLong( a.value ) )
  }

  implicit val slackDateTimeDecoder: Decoder[SlackDateTime] = (c: HCursor) => {
    c.as[Long].map( toSlackDateTime )
  }

}
