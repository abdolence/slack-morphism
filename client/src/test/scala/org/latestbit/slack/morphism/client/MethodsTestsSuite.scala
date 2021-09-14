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

import org.latestbit.slack.morphism.client.reqresp.test._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.Succeeded
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import sttp.client3.testing.SttpBackendStub
import io.circe.Encoder
import io.circe.generic.auto._
import org.latestbit.slack.morphism.client.reqresp.apps._
import org.scalatest.compatible.Assertion
import cats.implicits._

import scala.concurrent.Future

case class Data( a: String, b: String, c: String )

class MethodsTestsSuite extends AsyncFlatSpec with ScalaCheckDrivenPropertyChecks with SlackApiClientTestsSuiteSupport {

  def genBoundedGenList[T]( maxSize: Int, g: Gen[T] ): Gen[List[T]] = {
    Gen.choose( 1, maxSize ) flatMap { sz => Gen.listOfN( sz, g ) }
  }

  def testSlackApiMethod[RQ, RS](
      apiMethodCall: RQ => SttpBackendStub[Future, Any] => Future[Either[SlackApiClientError, RS]]
  )( responseFactory: RQ => RS )(
      reqRespAssertion: ( RQ, Either[SlackApiClientError, RS] ) => Assertion
  )( implicit arq: Arbitrary[RQ], encoder: Encoder.AsObject[RS] ) = {

    genBoundedGenList( 6, arq.arbitrary ).sample match {
      case Some( samples ) => {
        samples
          .map { sample =>
            val mockBackend =
              SttpBackendStub.asynchronousFuture.whenAnyRequest
                .thenRespondF(
                  createJsonResponseStub[RS]( responseFactory( sample ) )
                )
            ( sample, apiMethodCall( sample )( mockBackend ) )
          }
          .traverse { case ( sample, fa ) =>
            fa.map( ( sample, _ ) )
          }
          .map { samplesWithRes =>
            assert(
              samplesWithRes.forall { case ( request, response ) =>
                reqRespAssertion( request, response ) == Succeeded
              }
            )
          }
      }

      case _ => fail( "Empty samples are received" )
    }
  }

  "A Slack client" should "able to execute API test methods" in {

    implicit val arbString: Arbitrary[String] = Arbitrary( Gen.hexStr )

    implicitly[Arbitrary[SlackApiTestRequest]]

    testSlackApiMethod { req: SlackApiTestRequest => implicit backend =>
      val slackApiClient = SlackApiClient.create[Future]()
      slackApiClient.api.test( req )
    } { req: SlackApiTestRequest => SlackApiTestResponse( args = req.args ) } {
      case ( req, eitherResp ) => {
        assert(
          eitherResp.exists( req.args == _.args )
        )
      }

    }

  }

  "A Slack client" should "able to execute API 'apps' methods" in {
    implicitly[Arbitrary[SlackApiUninstallRequest]]

    testSlackApiMethod { req: SlackApiUninstallRequest => implicit backend =>
      val slackApiClient = SlackApiClient.create[Future]()
      slackApiClient.apps.uninstall( req )
    } { req => SlackApiUninstallResponse() } {
      case ( req, eitherResp ) => {
        assert(
          eitherResp.nonEmpty
        )
      }

    }

  }

}
