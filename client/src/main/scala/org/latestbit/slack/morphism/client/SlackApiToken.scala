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

import org.latestbit.slack.morphism.common.{ SlackAccessTokenValue, SlackTeamId }

/**
 * Slack API token base representation
 *
 * @group TokenDefs
 */
trait SlackApiToken {

  /**
   * Token value
   */
  val accessToken: SlackAccessTokenValue

  /**
   * Slack scope represented as a set of permissions
   */
  val scopeSet: Set[String]

  /**
   * Workspace/team id
   */
  val teamId: Option[SlackTeamId]

}

/**
 * Slack API token utility constructors and constants
 * @group TokenDefs
 */
object SlackApiToken {

  /**
   * Convert scope defined as a str to set of permissions
   * @param scopeAsStr scope as a str
   * @return set of permissions
   */
  private[client] def scopeToSet( scopeAsStr: Option[String] ): Set[String] = {
    scopeAsStr
      .map( _.split( ',' ).map( _.trim ).filterNot( _.isEmpty ).toSet )
      .getOrElse( Set() )
  }

  /**
   * Type names of tokens
   */
  object TokenTypes {
    final val Bot  = "bot"
    final val User = "user"
  }

  /**
   * Create an API token instance provided its type, value and scope
   * @param tokenType a token type
   * @param tokenValue a token value
   * @param scope a token scope
   * @param teamId a workspace/team id for this token
   * @return an API token instance for Slack API client
   */
  def createFrom(
      tokenType: String,
      tokenValue: SlackAccessTokenValue,
      scope: Option[String] = None,
      teamId: Option[SlackTeamId] = None
  ): Option[SlackApiToken] = {
    tokenType match {
      case TokenTypes.Bot  => Some( SlackApiBotToken( tokenValue, scope, teamId ) )
      case TokenTypes.User => Some( SlackApiUserToken( tokenValue, scope, teamId ) )
      case _               => None
    }
  }
}

/**
 * Slack API user token
 * @group TokenDefs
 *
 * @param accessToken token value
 * @param scope token scope in a string form
 * @param teamId a workspace/team id for this token
 */
case class SlackApiUserToken(
    override val accessToken: SlackAccessTokenValue,
    scope: Option[String] = None,
    teamId: Option[SlackTeamId] = None
) extends SlackApiToken {
  override val scopeSet: Set[String] = SlackApiToken.scopeToSet( scope )
}

/**
 * Slack API bot token
 * @group TokenDefs
 *
 * @param accessToken token value
 * @param scope token scope in a string form
 * @param teamId a workspace/team id for this token
 */
case class SlackApiBotToken(
    override val accessToken: SlackAccessTokenValue,
    scope: Option[String] = None,
    teamId: Option[SlackTeamId] = None
) extends SlackApiToken {
  override val scopeSet: Set[String] = SlackApiToken.scopeToSet( scope )
}
