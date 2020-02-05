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

import java.util.concurrent.{ Executors, TimeUnit }

import cats.implicits._

import org.latestbit.slack.morphism.concurrent.AsyncSeqIterator
import org.scalacheck._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.{ ExecutionContext, Future }

class AsyncSeqIteratorTestsSuite extends AsyncFlatSpec with ScalaCheckDrivenPropertyChecks {

  case class MyItem( value: String, cursor: Option[Int] )

  def genBoundedGenList[T]( maxSize: Int, g: Gen[T] ): Gen[List[T]] = {
    Gen.choose( 1, maxSize ) flatMap { sz =>
      Gen.listOfN( sz, g )
    }
  }

  def initialItem(): Future[MyItem] = Future.successful(
    MyItem( "initial", Some( 1 ) )
  )

  def nextItem( position: Int ): Future[MyItem] = {
    if (position < 10) {
      Future.successful(
        MyItem( s"next: ${position}", Some( position + 1 ) )
      )
    } else {
      Future.successful(
        MyItem( s"last", None )
      )
    }
  }

  def createIterator()( implicit ec: ExecutionContext ): AsyncSeqIterator[Future, MyItem, String] = {
    AsyncSeqIterator.cons[MyItem, String, Int](
      initial = initialItem(),
      toValue = _.value,
      getPos = _.cursor,
      producer = nextItem
    )( ec )
  }

  "iterating over generated async results" should "be in correct order" in {
    createIterator()
      .foldLeft( List[String]() ) {
        case ( all, itemValue ) =>
          all :+ itemValue
      }
      .map { xs =>
        assert( xs.nonEmpty )
        assert( xs.headOption.contains( "initial" ) )
        assert( xs.lastOption.contains( "last" ) )
        assert( xs.drop( 1 ).dropRight( 1 ).zipWithIndex.forall {
          case ( x, idx ) =>
            x.contains( s"next: ${idx + 1}" )
        } )
      }
  }

  "AsyncIterator" should "provide a map function" in {
    createIterator()
      .map( _.toUpperCase )
      .foldLeft( List[String]() ) {
        case ( all, itemValue ) =>
          all :+ itemValue
      }
      .map { xs =>
        assert( xs.nonEmpty )
        assert( xs.headOption.contains( "initial".toUpperCase ) )
        assert( xs.lastOption.contains( "last".toUpperCase ) )
        assert( xs.drop( 1 ).dropRight( 1 ).zipWithIndex.forall {
          case ( x, idx ) =>
            x.contains( s"next: ${idx + 1}".toUpperCase )
        } )
      }
  }

  "AsyncIterator" should "provide a foreach function" in {
    var testEffect: List[String] = List()
    val testExecutor = Executors.newSingleThreadExecutor()
    val testExecutorContext = ExecutionContext.fromExecutor( testExecutor )
    createIterator()( testExecutorContext ).foreach { testIterRes =>
      testEffect = testEffect :+ testIterRes
      if (testIterRes == "last") {
        testExecutor.shutdown()
      }
    }

    testExecutor.awaitTermination( 60, TimeUnit.SECONDS )

    assert( testEffect.nonEmpty )
    assert( testEffect.headOption.contains( "initial" ) )
    assert( testEffect.lastOption.contains( "last" ) )
    assert( testEffect.drop( 1 ).dropRight( 1 ).zipWithIndex.forall {
      case ( x, idx ) =>
        x.contains( s"next: ${idx + 1}" )
    } )
  }

  "AsyncIterator" should "provide a cats Functor instance" in {
    createIterator()
      .fmap( _.toUpperCase )
      .fproduct( _.length )
      .foldLeft( List[String]() ) {
        case ( all, ( itemValue, _ ) ) =>
          all :+ itemValue
      }
      .map { xs =>
        assert( xs.nonEmpty )
        assert( xs.headOption.contains( "initial".toUpperCase ) )
      }
  }

}
