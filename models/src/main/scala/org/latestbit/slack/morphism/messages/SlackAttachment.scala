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

package org.latestbit.slack.morphism.messages

import io.circe._

import org.latestbit.circe.adt.codec._

sealed trait SlackAttachment
case class TestSlackAttachment() extends SlackAttachment

object SlackAttachment {

  import io.circe.generic.semiauto._
  import io.circe.syntax._

  implicit val encoderTestSlackAttachment: Encoder.AsObject[TestSlackAttachment] = deriveEncoder[TestSlackAttachment]
  implicit val decoderTestSlackAttachment: Decoder[TestSlackAttachment] = deriveDecoder[TestSlackAttachment]

  implicit val encoder: Encoder[SlackAttachment] = JsonTaggedAdtCodec.createEncoder[SlackAttachment]( "type" )
  implicit val decoder: Decoder[SlackAttachment] = JsonTaggedAdtCodec.createDecoder[SlackAttachment]( "type" )

}
