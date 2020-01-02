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

class SlackEventSignatureVerifier() {
  import SlackEventSignatureVerifier._

  private def signDataWithKeySecret( mac: Mac, signingSecret: String, dataToSign: String ): Array[Byte] = {
    val secretKeySpec = new SecretKeySpec( signingSecret.getBytes(), mac.getAlgorithm )
    mac.init( secretKeySpec )
    mac.doFinal( dataToSign.getBytes )
  }

  def verify(
      signingSecret: String,
      receivedHash: String,
      timestamp: String,
      body: String
  ): Either[SlackSignatureVerificationError, SlackSignatureVerificationSuccess] = {
    Try( Mac.getInstance( SIGNING_ALGORITHM ) ).toEither
      .flatMap { mac =>
        val dataToSign = s"v0:${timestamp}:${body}"
        Try( signDataWithKeySecret( mac, signingSecret, dataToSign ) ).toEither.flatMap { signedBytes =>
          val generatedHash = s"v0=${signedBytes.toHexString()}"
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
      .leftMap { ex =>
        SlackSignatureCryptoInitError( ex )
      }
  }
}

object SlackEventSignatureVerifier {
  val SIGNING_ALGORITHM = "HmacSHA256"

  object HttpHeaderNames {
    val SIGNED_HASH = "X-Slack-Signature"
    val SIGNED_TIMESTAMP = "X-Slack-Request-Timestamp"
  }

}
