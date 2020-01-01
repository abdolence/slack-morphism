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

package org.latestbit.slack.morphism.client

import org.scalacheck._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.latestbit.slack.morphism.ext.ArrayExt._

class ArrayExtTestsSuite extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  private def b2hf( bytes: Array[Byte], sep: String = "" ): String =
    bytes.map( "%02x".format( _ ) ).mkString( sep )

  implicit val arbArrayOfBytes: Arbitrary[Array[Byte]] = Arbitrary( Gen.containerOf[Array, Byte]( Gen.posNum[Byte] ) )

  forAll { testArray: Array[Byte] =>
    val testHexString = b2hf( testArray )
    assert( testHexString === testArray.toHexString() )
  }

}
