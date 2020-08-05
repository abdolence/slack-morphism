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

package org.latestbit.slack.morphism.events.signature

import cats.implicits._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.latestbit.slack.morphism.ext.ArrayExt._

import scala.util.Try

/**
 * Slack push/callback events signature verifier
 */
class SlackEventSignatureVerifier() {
  import SlackEventSignatureVerifier._

  private def signStrWithKeySecret( mac: Mac, signingSecret: String, dataToSign: String ): Array[Byte] = {
    val secretKeySpec = new SecretKeySpec( signingSecret.getBytes(), mac.getAlgorithm )
    mac.init( secretKeySpec )
    mac.doFinal( dataToSign.getBytes )
  }

  private[signature] def signData(
      signingSecret: String,
      timestamp: String,
      body: String
  ): Either[SlackSignatureCryptoInitError, String] = {
    Try( Mac.getInstance( SigningAlgorithm ) ).toEither
      .flatMap { mac =>
        val dataToSign = s"v0:${timestamp}:${body}"
        Try( signStrWithKeySecret( mac, signingSecret, dataToSign ) ).toEither.map { signedBytes =>
          s"v0=${signedBytes.toHexString()}"
        }
      }
      .leftMap { ex => SlackSignatureCryptoInitError( ex ) }
  }

  /**
   * Verify a signature for an event
   * @param signingSecret a secret from you Slack profile
   * @param receivedHash a hash received from Slack HTTP param
   * @param timestamp a timestamp received from Slack HTTP param
   * @param body an event body
   * @return either an success or verification error with details
   */
  def verify(
      signingSecret: String,
      receivedHash: String,
      timestamp: String,
      body: String
  ): Either[SlackSignatureVerificationError, SlackSignatureVerificationSuccess] = {
    signData( signingSecret, timestamp, body ).flatMap { generatedHash =>
      if (generatedHash != receivedHash) {
        SlackSignatureWrongSignatureError(
          receivedHash = receivedHash,
          generatedHash = generatedHash,
          timestamp = timestamp
        ).asLeft
      } else
        SlackSignatureVerificationSuccess().asRight
    }
  }
}

object SlackEventSignatureVerifier {

  /**
   * Slack Events API signature verification algorithm
   */
  final val SigningAlgorithm = "HmacSHA256"

  /**
   * Slack Events API HTTP header names with values to verify signature
   */
  object HttpHeaderNames {
    final val SignedHash = "x-slack-signature"
    final val SignedTimestamp = "x-slack-request-timestamp"
  }

}
