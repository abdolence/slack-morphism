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
package org.latestbit.slack.morphism.client

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.client._

class SlackApiTokenTestsSuite extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks {

  forAll( arbitrary[String].suchThat( !_.isEmpty ) ) { tokenValueStr: String =>
    val tokenValue = SlackAccessTokenValue( tokenValueStr )

    val userScope = SlackApiTokenScope( "test1:test11,test2,test3:test33" )
    val userToken = SlackApiUserToken(
      accessToken = tokenValue,
      scope = Some( userScope )
    )

    val botScope = SlackApiTokenScope( "test2" )
    val botToken = SlackApiBotToken(
      accessToken = tokenValue,
      scope = Some( botScope )
    )

    val findToken1 = SlackApiToken.findFirst[SlackApiBotToken]( List( userToken, botToken ), SlackApiTokenType.Bot )

    assert( findToken1 == Some( botToken ) )
  }

}
