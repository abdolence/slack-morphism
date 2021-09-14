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

package org.latestbit.slack.morphism.examples.akka.db

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import swaydb.{ Set => _, _ }
import swaydb.serializers.Default._
import swaydb.data.Functions
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.scalalogging._
import swaydb.data.slice.Slice
import cats.implicits._
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.examples.akka.config.AppConfig
import swaydb.serializers.Serializer

import scala.concurrent.{ ExecutionContextExecutor, Future }

object SlackTokensDb extends StrictLogging {

  case class TokenRecord(
      tokenType: SlackApiTokenType,
      tokenValue: SlackAccessTokenValue,
      userId: SlackUserId,
      scope: SlackApiTokenScope
  )
  case class TeamTokensRecord( teamId: SlackTeamId, tokens: List[TokenRecord] )

  implicit object TeamTokensRecordSwayDbSerializer extends swaydb.serializers.Serializer[TeamTokensRecord] {
    import io.circe.parser._
    import io.circe.syntax._
    import io.circe.generic.auto._

    override def write( data: TeamTokensRecord ): Slice[Byte] = {
      Slice( data.asJson.dropNullValues.noSpaces.getBytes )
    }

    override def read( data: Slice[Byte] ): TeamTokensRecord = {
      decode[TeamTokensRecord]( new String( data.toArray ) ).valueOr( throw _ )
    }
  }

  sealed trait Command
  case class OpenDb( config: AppConfig )                                                                 extends Command
  case class Close()                                                                                     extends Command
  case class InsertToken( teamId: SlackTeamId, tokenRecord: TokenRecord )                                extends Command
  case class RemoveTokens( teamId: SlackTeamId, users: Set[SlackUserId] )                                extends Command
  case class ReadTokens( teamId: SlackTeamId, ref: akka.actor.typed.ActorRef[Option[TeamTokensRecord]] ) extends Command

  type FunctionType = PureFunction[SlackTeamId, TeamTokensRecord, Apply.Map[TeamTokensRecord]]
  type SwayDbType   = Future[Map[SlackTeamId, TeamTokensRecord, FunctionType, Future]]

  val run: Behavior[Command] = runBehavior( None )

  private def runBehavior( swayMap: Option[SwayDbType] ): Behavior[Command] = {
    Behaviors.setup { implicit context =>
      implicit val system                       = context.system
      implicit val classicSystem                = context.system.toClassic
      implicit val ec: ExecutionContextExecutor = context.system.executionContext

      implicit val teamIdSerializer = new Serializer[SlackTeamId] {
        override def write( data: SlackTeamId ): Slice[Byte] = StringSerializer.write( data.value )

        override def read( data: Slice[Byte] ): SlackTeamId = SlackTeamId( StringSerializer.read( data ) )
      }

      Behaviors.receiveMessage {
        case OpenDb( config ) => {
          logger.info( s"Opening sway db in ${config.databaseDir}" )
          implicit val functions = Functions[PureFunction.Map[SlackTeamId, TeamTokensRecord]]()

          val map: SwayDbType =
            persistent.Map[SlackTeamId, TeamTokensRecord, FunctionType, Future]( dir = config.databaseDir )

          runBehavior( Some( map ) )
        }

        case InsertToken( teamId: SlackTeamId, tokenRecord ) => {
          swayMap.foreach { swayMapFuture =>
            swayMapFuture.foreach { storageMap =>
              storageMap
                .get( key = teamId )
                .map(
                  _.map( rec =>
                    rec.copy( tokens = rec.tokens.filterNot( _.userId == tokenRecord.userId ) :+ tokenRecord )
                  ).getOrElse(
                    TeamTokensRecord(
                      teamId = teamId,
                      tokens = List(
                        tokenRecord
                      )
                    )
                  )
                )
                .foreach { record =>
                  logger.debug( s"Inserting record for : ${teamId}/${tokenRecord.userId}" )
                  storageMap.put( teamId, record )
                }
            }
          }
          Behaviors.same
        }

        case ReadTokens( teamId, ref ) => {
          swayMap.foreach( _.foreach( _.get( key = teamId ).foreach { record => ref ! record } ) )
          Behaviors.same
        }

        case RemoveTokens( teamId: SlackTeamId, users: Set[SlackUserId] ) => {
          swayMap.foreach( _.foreach { swayMap =>
            swayMap
              .get( key = teamId )
              .foreach( _.foreach { record =>
                swayMap.put(
                  teamId,
                  record.copy(
                    tokens = record.tokens.filterNot( token => users.contains( token.userId ) )
                  )
                )
              } )
          } )
          Behaviors.same
        }

        case Close() => {
          Behaviors.stopped
        }
      }
    }

  }
}
