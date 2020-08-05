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

package org.latestbit.slack.morphism.client.tests

import cats.effect.IO
import org.latestbit.slack.morphism.client.{ SlackApiClientError, SlackApiSystemError }
import org.latestbit.slack.morphism.client.streaming.{ SlackApiResponseScroller, SlackApiScrollableResponse }

import scala.concurrent.{ ExecutionContext, Future }
import cats.instances.future._

case class TestScrollableResponse(
    values: List[Int],
    cursor: Option[String]
) extends SlackApiScrollableResponse[Int, String] {

  override def items: List[Int]             = values
  override def getLatestPos: Option[String] = cursor
}

object TestScrollableResponse {

  val testBatches: Seq[TestScrollableResponse] = IndexedSeq(
    TestScrollableResponse(
      values = ( 0 to 9).toList,
      cursor = Some( "1" )
    ),
    TestScrollableResponse(
      values = ( 10 to 19).toList,
      cursor = Some( "2" )
    ),
    TestScrollableResponse(
      values = ( 20 to 29).toList,
      cursor = Some( "3" )
    ),
    TestScrollableResponse(
      values = ( 30 to 39).toList,
      cursor = None
    )
  )

  val testFoldedBatchesResults = testBatches.flatMap( _.values ).toList

  def createTestScrollableResponse()( implicit
      ec: ExecutionContext
  ): SlackApiResponseScroller[Future, Int, String, TestScrollableResponse] = {

    def initialResponseLoader(): Future[Either[SlackApiClientError, TestScrollableResponse]] = {
      Future {
        testBatches.headOption
          .map( Right.apply )
          .getOrElse(
            Left( SlackApiSystemError( null, new IllegalStateException( "Empty head?" ) ) )
          )
      }( scala.concurrent.ExecutionContext.Implicits.global )
    }

    def responseScroller(
        cursor: String
    ): Future[Either[SlackApiClientError, TestScrollableResponse]] = {
      Future {
        testBatches
          .lift( cursor.toInt )
          .map( Right.apply )
          .getOrElse(
            Left(
              SlackApiSystemError(
                null,
                new IllegalArgumentException( s"Illegal cursor: ${cursor}" )
              )
            )
          )
      }( scala.concurrent.ExecutionContext.Implicits.global )
    }

    new SlackApiResponseScroller[Future, Int, String, TestScrollableResponse](
      initialResponseLoader _,
      responseScroller
    )
  }

  def createIOTestScrollableResponse(): SlackApiResponseScroller[IO, Int, String, TestScrollableResponse] = {

    def initialResponseLoader(): IO[Either[SlackApiClientError, TestScrollableResponse]] = {
      IO {
        testBatches.headOption
          .map( Right.apply )
          .getOrElse(
            Left( SlackApiSystemError( null, new IllegalStateException( "Empty head?" ) ) )
          )
      }
    }

    def responseScroller(
        cursor: String
    ): IO[Either[SlackApiClientError, TestScrollableResponse]] = {
      IO {
        testBatches
          .lift( cursor.toInt )
          .map( Right.apply )
          .getOrElse(
            Left(
              SlackApiSystemError(
                null,
                new IllegalArgumentException( s"Illegal cursor: ${cursor}" )
              )
            )
          )
      }
    }

    new SlackApiResponseScroller[IO, Int, String, TestScrollableResponse](
      initialResponseLoader _,
      responseScroller
    )
  }
}
