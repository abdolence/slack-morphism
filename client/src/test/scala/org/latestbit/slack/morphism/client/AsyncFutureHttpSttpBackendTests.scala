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

import org.latestbit.slack.morphism.client.reqresp.test.SlackApiTestRequest
import org.scalatest.flatspec.AsyncFlatSpec
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend

class AsyncFutureHttpSttpBackendTests extends AsyncFlatSpec with SlackApiClientTestsSuiteSupport {
  "A Slack client" should "able to try to connect using a async http client network sttp backend" in {
    implicit val sttpBackend = AsyncHttpClientFutureBackend()
    val slackApiClient = new SlackApiClient()

    slackApiClient.api.test( SlackApiTestRequest() ).map {
      case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
      case Left( ex: SlackApiResponseError ) => assert( ex.errorCode !== null )
      case Left( ex )                        => fail( ex )
    }
  }

  /*
  Maybe for future releases
  it should "able to try to connect using a async http cats effect network sttp backend" in {
    //implicit val sttpBackend = AsyncHttpClientCatsBackend()
    val slackApiClient = new SlackApiClient()

    slackApiClient.api.test( SlackApiTestRequest() ).map {
      case Right( resp )                     => fail( s"Unexpected resp: ${resp}" )
      case Left( ex: SlackApiResponseError ) => assert( ex.errorCode !== null )
      case Left( ex )                        => fail( ex )
    }
  }*/
}
