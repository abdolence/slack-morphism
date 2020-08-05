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

package org.latestbit.slack.morphism.common

import io.circe._
import io.circe.parser._
import io.circe.syntax._

/**
 *  Defines an auxiliary data type for images in different dimensions
 */
case class SlackIcon(
    image_original: Option[String] = None,
    image_default: Option[Boolean] = None,
    images: List[( Int, String )] = List()
)

object SlackIcon {

  final val IconPrefix = "image_"

  implicit val slackUserInfoEncoder: Encoder.AsObject[SlackIcon] = ( model: SlackIcon) => {
    val baseObject =
      JsonObject(
        "image_original" -> model.image_original.asJson,
        "image_default"  -> model.image_default.asJson
      )

    model.images.foldLeft( baseObject ) {
      case ( wholeObj, ( res, link ) ) =>
        wholeObj.add(
          s"${IconPrefix}${res}",
          link.asJson
        )
    }
  }

  implicit val slackUserInfoDecoder: Decoder[SlackIcon] = ( cursor: HCursor) => {

    def cursorToImages(): List[( Int, String )] = {
      cursor.keys
        .map { cursorKeys =>
          cursorKeys
            .filter { key => key.startsWith( IconPrefix ) }
            .flatMap { key =>
              key.split( '_' ).toList match {
                case _ :: res :: _ if res.forall( _.isDigit ) => {
                  cursor.downField( key ).as[String].toOption.map( value => ( res.toInt, value ) )
                }
                case _ => None
              }
            }
            .toList
        }
        .getOrElse( List() )
    }

    for {
      image_original <- cursor.downField( "image_original" ).as[Option[String]]
      image_default  <- cursor.downField( "image_default" ).as[Option[Boolean]]
    } yield SlackIcon(
      image_original,
      image_default,
      cursorToImages()
    )
  }

}
