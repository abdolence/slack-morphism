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

package org.latestbit.slack.morphism.coding

import io.circe.{ Decoder, Encoder }
import io.circe.syntax._
import io.circe.parser._
import org.latestbit.circe.adt.codec._
import org.scalatest.flatspec.AnyFlatSpec

case class TestModel( f1: String, f2: Long = 5, f3: Option[String] = None, f4: Option[Long] = None )

sealed trait TestEvent

@JsonAdt( "ev1" )
case class TestEvent1( f1: String ) extends TestEvent

@JsonAdt( "ev2" )
case class TestEvent2( f1: String ) extends TestEvent

class JsonCodingTestSuite extends AnyFlatSpec {

  private val testModelNoOpt = TestModel(
    f1 = "test-client"
  )

  private val testModelWithOpts = testModelNoOpt.copy(
    f3 = Some( "test-name" ),
    f4 = Some( 10 )
  )

  "A simple model" should "be serialised and deserialised correctly" in {
    Seq( testModelNoOpt, testModelWithOpts ).foreach { testModel =>
      import io.circe.generic.auto._
      val json = testModel.asJson.dropNullValues.noSpaces

      decode[TestModel](
        json
      ) match {
        case Right( model ) => assert( model === testModel )
        case Left( ex )     => fail( ex )
      }
    }
  }

  it should "be able to ignore unknown fields to decode" in {
    import io.circe.generic.auto._
    decode[TestModel](
      """{ "f1" : "test", "f2" : 100, "fx" : "extended" }"""
    ) match {
      case Right( model ) => assert( model === TestModel( f1 = "test", f2 = 100 ) )
      case Left( ex )     => fail( ex )
    }

  }

  "ADT model" should "be serialised and deserialised correctly with ADT codec" in {
    import io.circe.generic.auto._

    implicit val encoder: Encoder[TestEvent] = JsonTaggedAdtCodec.createEncoder[TestEvent]( "type" )
    implicit val decoder: Decoder[TestEvent] = JsonTaggedAdtCodec.createDecoder[TestEvent]( "type" )

    val testModel: TestEvent = TestEvent1( "test-data" )
    val json = testModel.asJson.dropNullValues.noSpaces

    decode[TestEvent](
      json
    ) match {
      case Right( model ) => assert( model === testModel )
      case Left( ex )     => fail( ex )
    }
  }

}
