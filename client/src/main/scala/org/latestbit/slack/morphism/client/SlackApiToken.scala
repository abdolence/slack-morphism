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

/**
 * Slack API token base representation
 */
trait SlackApiToken {

  /**
	 * Token value
	 */
  val value: String

  /**
	 * Slack scope represented as a set of permissions
	 */
  val scopeSet: Set[String]
}

object SlackApiToken {

  /**
   * Convert scope defined as a str to set of permissions
   * @param scopeAsStr scope as a str
   * @return set of permissions
   */
  def scopeToSet( scopeAsStr: String ): Set[String] = {
    Option( scopeAsStr )
      .map( _.split( ',' ).map( _.trim ).filterNot( _.isEmpty ).toSet )
      .getOrElse( Set() )
  }

  /**
   * Type names of tokens
   */
  object TokenTypes {
    val BOT = "bot"
    val USER = "user"
  }

  /**
   * Create an API token instance provided its type, value and scope
   * @param tokenType a token type
   * @param tokenValue a token value
   * @param scope a token scope
   * @return an API token instance for Slack API client
   */
  def createFrom( tokenType: String, tokenValue: String, scope: String ): Option[SlackApiToken] = {
    tokenType match {
      case TokenTypes.BOT  => Some( SlackApiBotToken( tokenValue, scope ) )
      case TokenTypes.USER => Some( SlackApiUserToken( tokenValue, scope ) )
      case _               => None
    }
  }
}

/**
 * Slack API user token
 * @param value token value
 * @param scope token scope in a string form
 */
case class SlackApiUserToken( override val value: String, scope: String ) extends SlackApiToken {
  override val scopeSet: Set[String] = SlackApiToken.scopeToSet( scope )
}

/**
 * Slack API bot token
 * @param value token value
 * @param scope token scope in a string form
 */
case class SlackApiBotToken( override val value: String, scope: String ) extends SlackApiToken {
  override val scopeSet: Set[String] = SlackApiToken.scopeToSet( scope )
}
