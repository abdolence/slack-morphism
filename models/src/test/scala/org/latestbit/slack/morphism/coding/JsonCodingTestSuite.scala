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

import java.time.Instant

import io.circe.{ Decoder, Encoder }
import io.circe.syntax._
import io.circe.parser._
import org.latestbit.circe.adt.codec._
import org.latestbit.slack.morphism.client.models.common.SlackDateTime
import org.latestbit.slack.morphism.client.models.messages.SlackUserMessage
import org.scalatest.flatspec.AnyFlatSpec

case class TestModel( f1: String, f2: Long = 5, f3: Option[String] = None, f4: Option[Long] = None )

sealed trait TestEvent

@JsonAdt( "ev1" )
case class TestEvent1( f1: String ) extends TestEvent

@JsonAdt( "ev2" )
case class TestEvent2( f1: String ) extends TestEvent

case class TestSlackInstant(
    simpleLong: Long,
    instant: Instant,
    slackTime: SlackDateTime,
    optSlackTime: Option[SlackDateTime],
    optSlackTimeAbsent: Option[SlackDateTime] = None
)

case class TestBool(
    b1: Boolean,
    b2: Boolean = true,
    b3: Option[Boolean] = None,
    b4: Option[List[Int]] = None
)

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

  "A Slack response with instants" should "be serialised and deserialized correctly" in {
    import io.circe.generic.auto._

    val testTime = Instant.now()

    val testModel = TestSlackInstant(
      testTime.toEpochMilli,
      testTime,
      slackTime = testTime,
      optSlackTime = Some( testTime )
    )

    val testJson = testModel.asJson.dropNullValues.noSpaces

    assert( !(testJson contains "value") )

    decode[TestSlackInstant](
      testJson
    ) match {
      case Right( model ) => {
        assert( model.slackTime.value.getEpochSecond === testTime.getEpochSecond )
        assert( model.optSlackTime.exists( _.value.getEpochSecond === testTime.getEpochSecond ) )
      }
      case Left( ex ) => fail( ex )
    }
  }

  "A JSON model with primitive types like booleans" should "respect default values" in {
    import io.circe.generic.auto._

    val testIncorrectJson = """{}"""
    val testJson = """{ "b1" : false, "b2" : true }"""

    decode[TestBool](
      testIncorrectJson
    ) match {
      case Right( model ) => {
        fail( model.toString() )
      }
      case Left( _ ) => {}
    }

    decode[TestBool](
      testJson
    ) match {
      case Right( model ) => {
        assert( model === TestBool( b1 = false ) )
      }
      case Left( err ) => fail( err )
    }

  }

  "A JSON/ADT model for SlackMessage" should "be available implicitly" in {
    import org.latestbit.slack.morphism.client.models.messages.SlackMessage

    val testModel: SlackMessage = org.latestbit.slack.morphism.client.models.messages.SlackUserMessage(
      ts = "test",
      channel = "test-channek",
      text = "Test Text",
      user = "test-user"
    )

    val testJson = testModel.asJson.dropNullValues.noSpaces

    assert( !(testJson contains "SlackUserMessage") )

    decode[SlackMessage](
      testJson
    ) match {
      case Right( model: SlackUserMessage ) => {
        assert( model === testModel )
      }
      case Left( ex ) => fail( ex )
    }
  }

}
