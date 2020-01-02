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

import org.scalatest.flatspec.AsyncFlatSpec

import scala.concurrent.Future

class QuickCheck extends AsyncFlatSpec {

  object Test {

    import cats._
    import cats.implicits._

    case class MyModel( value: String )

    def test1[A, F]( obj: A ): F = {
      obj.asInstanceOf[F]
    }

    def test2[F[_] : Applicative, A]( obj: A ): F[A] = {
      val f = Applicative[F]
      f.pure( obj )
    }

    def test3( x: String, y: String ): MyModel = {
      MyModel( s"${x}-${y}" )
    }

    def check() = {
      println( test1[MyModel, MyModel]( MyModel( "S" ) ) )
      //println(test2[MyModel,Option[MyModel]](MyModel("S")))

      val res1 = test2[Option, MyModel]( MyModel( "S" ) )
      println( res1 )

      val res2 = test2[List, MyModel]( MyModel( "S" ) )
      println( res2 )

      val testOptStr1 = Option( "test 1" )
      val testOptStr2 = Option( "test 2" )

      val mappedXs = ( testOptStr1, testOptStr2 ).mapN( test3 )

      val xs: Future[List[MyModel]] =
        List( Future { MyModel( "test1" ) }, Future { MyModel( "test2" ) } )
          .traverse { xo: Future[MyModel] =>
            xo
          }

      println( xs )

      val testList = List[Option[MyModel]]( Some( MyModel( "S" ) ), Some( MyModel( "A" ) ) )

      val collectedTestList = testList.collect {
        case Some( MyModel( x ) ) if x == "S" => x
      }

      println( collectedTestList )

    }
  }

  Test.check()

}
