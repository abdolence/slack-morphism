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

package org.latestbit.slack.morphism.ext

import scala.language.implicitConversions

object ArrayExt {

  private final val DigitsLower =
    Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' )
  private final val DigitsUpper = DigitsLower.map( _.toUpper )

  trait ArrayBytesSupport[T] {
    def toByte( value: T ): Byte
  }

  implicit object ArrayOfBytes extends ArrayBytesSupport[Byte] {
    override def toByte( value: Byte ): Byte = value
  }

  implicit class ArrayExtensions[T]( val array: Array[T] ) extends AnyVal {

    /**
     * Convert an array to hex string
     */
    @inline final def toHexString( lowerCased: Boolean = true )( implicit abEv: ArrayBytesSupport[T] ): String = {
      val outputArrayLen = array.length << 1
      val outputArray    = new Array[Char]( outputArrayLen )

      val currentDigits =
        if (lowerCased)
          DigitsLower
        else
          DigitsUpper

      array.indices.zip( outputArray.indices.by( 2 ) ).foreach { case ( i, j ) =>
        val currentByte = abEv.toByte( array( i ) )
        outputArray( j ) = currentDigits( ( 0xf0 & currentByte) >>> 4 )
        outputArray( j + 1 ) = currentDigits( 0x0f & currentByte )
      }
      new String( outputArray )

    }
  }

}
