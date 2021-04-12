package org.latestbit.slack.morphism.common

import cats.implicits._
import io.circe.syntax._
import io.circe._

case class SlackFileThumbnails(
    thumbs: List[( String, String )] = List()
)

object SlackFileThumbnails {

  final val ThumbsPrefix = "thumb_"

  implicit val slackFileThumbNailsEncoder: Encoder.AsObject[SlackFileThumbnails] = (model: SlackFileThumbnails ) => {
    val baseObject =
      JsonObject()

    model.thumbs.foldLeft( baseObject ) { case ( wholeObj, ( res, link ) ) =>
      wholeObj.add(
        s"${ThumbsPrefix}${res}",
        link.asJson
      )
    }
  }

  implicit val slackFileThumbNailsDecoder: Decoder[SlackFileThumbnails] = (cursor: HCursor ) => {

    def cursorToImages(): List[( String, String )] = {
      cursor.keys
        .map { cursorKeys =>
          cursorKeys
            .filter { key => key.startsWith( ThumbsPrefix ) }
            .flatMap { key =>
              cursor.downField( key ).as[String].toOption.map( value => ( key.replace( ThumbsPrefix, "" ), value ) )

            }
            .toList
        }
        .getOrElse( List() )
    }

    SlackFileThumbnails(
      cursorToImages()
    ).asRight
  }

}
