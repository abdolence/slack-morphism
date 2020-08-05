/*
 * Copyright 2020 Abdulla Abdurakhmanov (abdulla@latestbit.com)
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

package org.latestbit.slack.morphism.events.signature

import org.scalacheck._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalacheck.ScalacheckShapeless._

class SlackEventSignatureVerifierTests extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  implicit val arbString: Arbitrary[String] = Arbitrary( Gen.hexStr )

  case class TestSigEvent( ts: String, body: String )
  implicitly[Arbitrary[TestSigEvent]]

  val signatureVerifier = new SlackEventSignatureVerifier()

  signatureVerifier.verify(
    "",
    "",
    "ds",
    "ds"
  ) match {
    case Right( _ )                               => fail()
    case Left( _: SlackSignatureCryptoInitError ) => succeed
    case Left( err )                              => fail( err )
  }

  val secret = Gen.hexStr.retryUntil( _.length > 3 ).sample.orNull

  assert( secret !== null )

  signatureVerifier.verify(
    secret,
    "",
    "ds",
    "ds"
  ) match {
    case Right( _ )                                   => fail()
    case Left( _: SlackSignatureWrongSignatureError ) => succeed
    case Left( err )                                  => fail( err )
  }

  signatureVerifier.verify(
    secret,
    "some-hash-data",
    "ds",
    "ds"
  ) match {
    case Right( _ )                                   => fail()
    case Left( _: SlackSignatureWrongSignatureError ) => succeed
    case Left( err )                                  => fail( err )
  }

  forAll { ev: TestSigEvent =>
    signatureVerifier.signData( secret, ev.ts, ev.body ) match {
      case Right( signedStr ) => {
        signatureVerifier.verify(
          secret,
          receivedHash = signedStr,
          timestamp = ev.ts,
          ev.body
        ) match {
          case Right( _ ) => succeed
          case Left( er ) => fail( er )
        }

      }
      case Left( er ) => fail( er )
    }
  }

  final val TestSecret = "d058b0b8f3f91e4446ad981890c9b6c16b2acc85367e30a2d76b8a95e525c02a";
  final val TestHash = "v0=37ca0519af8b621f18b13586fc72488ebb159fc730a5d1718dd823dec69dea95";
  final val TestBody = "test-body";
  final val TestTs = "test-ts";

  signatureVerifier.verify(
    TestSecret,
    receivedHash = TestHash,
    timestamp = TestTs,
    body = TestBody
  ) match {
    case Right( _ ) => succeed
    case Left( er ) => fail( er )
  }
}
