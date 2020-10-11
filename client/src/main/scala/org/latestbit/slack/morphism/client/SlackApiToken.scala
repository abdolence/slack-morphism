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

import org.latestbit.slack.morphism.common._
import cats.data.NonEmptyList

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
  val permissions: Set[SlackApiTokenScopePermission]

  /**
   * Workspace/team id
   */
  val teamId: Option[SlackTeamId]

  /**
   * Token type
   */
  val tokenType: SlackApiTokenType
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
  private[client] def scopeToPermissionSet( scope: Option[SlackApiTokenScope] ): Set[SlackApiTokenScopePermission] = {
    scope
      .map( _.value.split( ',' ).map( _.trim ).filterNot( _.isEmpty ).map( SlackApiTokenScopePermission.apply ).toSet )
      .getOrElse( Set() )
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
      tokenType: SlackApiTokenType,
      tokenValue: SlackAccessTokenValue,
      scope: Option[SlackApiTokenScope] = None,
      teamId: Option[SlackTeamId] = None
  ): SlackApiToken = {
    tokenType match {
      case SlackApiTokenType.Bot  => SlackApiBotToken( tokenValue, scope, teamId )
      case SlackApiTokenType.User => SlackApiUserToken( tokenValue, scope, teamId )
      case SlackApiTokenType.App  => SlackApiAppToken( tokenValue, scope, teamId )
    }
  }

  /**
   * Find a required token in the list by type
   *
   * @param tokens token list
   * @param tokenType a token has this typ
   * @param permissions a token has those permissions (optionally)
   */
  def findFirst[A <: SlackApiToken](
      tokens: Iterable[SlackApiToken],
      tokenType: SlackApiTokenType,
      permissions: Option[Set[SlackApiTokenScopePermission]] = None
  ): Option[A] = {
    tokens.collectFirst {
      case token if permissions.forall( _.subsetOf( token.permissions ) ) && token.tokenType == tokenType =>
        token.asInstanceOf[A]
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
    scope: Option[SlackApiTokenScope] = None,
    teamId: Option[SlackTeamId] = None
) extends SlackApiToken {
  override val permissions: Set[SlackApiTokenScopePermission] = SlackApiToken.scopeToPermissionSet( scope )
  override val tokenType: SlackApiTokenType                   = SlackApiTokenType.User
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
    scope: Option[SlackApiTokenScope] = None,
    teamId: Option[SlackTeamId] = None
) extends SlackApiToken {
  override val permissions: Set[SlackApiTokenScopePermission] = SlackApiToken.scopeToPermissionSet( scope )
  override val tokenType: SlackApiTokenType                   = SlackApiTokenType.Bot
}

/**
 * Slack API app token
 * @group TokenDefs
 *
 * @param accessToken token value
 * @param scope token scope in a string form
 * @param teamId a workspace/team id for this token
 */
case class SlackApiAppToken(
    override val accessToken: SlackAccessTokenValue,
    scope: Option[SlackApiTokenScope] = None,
    teamId: Option[SlackTeamId] = None
) extends SlackApiToken {
  override val permissions: Set[SlackApiTokenScopePermission] = SlackApiToken.scopeToPermissionSet( scope )
  override val tokenType: SlackApiTokenType                   = SlackApiTokenType.App
}
