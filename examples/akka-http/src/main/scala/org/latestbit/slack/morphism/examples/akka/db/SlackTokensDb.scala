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
import org.latestbit.slack.morphism.examples.akka.AppConfig
import swaydb._
import swaydb.serializers.Default._
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.scalalogging._
import swaydb.IO.ApiIO
import swaydb.data.slice.Slice
import cats.implicits._

import scala.concurrent.ExecutionContextExecutor

object SlackTokensDb extends StrictLogging {

  case class TokenRecord( tokenType: String, tokenValue: String, userId: String, scope: String )
  case class TeamTokensRecord( teamId: String, tokens: List[TokenRecord] )

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
  case class OpenDb( config: AppConfig ) extends Command
  case class Close() extends Command
  case class InsertToken( teamId: String, tokenRecord: TokenRecord ) extends Command
  case class RemoveToken( teamId: String, userId: String ) extends Command
  case class ReadTokens( teamId: String, ref: akka.actor.typed.ActorRef[Option[TeamTokensRecord]] ) extends Command

  type FunctionType = PureFunction[String, TeamTokensRecord, Apply.Map[TeamTokensRecord]]
  type SwayDbType = Map[String, TeamTokensRecord, FunctionType, ApiIO]

  val run: Behavior[Command] = runBehavior( None )

  private def runBehavior( swayMap: Option[SwayDbType] ): Behavior[Command] = {
    Behaviors.setup { implicit context =>
      implicit val system = context.system
      implicit val classicSystem = context.system.toClassic
      implicit val ec: ExecutionContextExecutor = context.system.executionContext

      Behaviors.receiveMessage {
        case OpenDb( config ) => {
          logger.info( s"Opening sway db in ${config.databaseDir}" )
          val map: SwayDbType =
            persistent.zero.Map[String, TeamTokensRecord, FunctionType, IO.ApiIO]( dir = config.databaseDir ).get

          runBehavior( Some( map ) )
        }

        case InsertToken( teamId: String, tokenRecord ) => {
          swayMap.foreach { swayMap =>
            swayMap
              .get( key = teamId )
              .map( _.map(rec => rec.copy( tokens = rec.tokens :+ tokenRecord ) ) )
              .getOrElse(
                Some(
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
                swayMap.put( teamId, record )
              }
          }
          Behavior.same
        }

        case ReadTokens( teamId, ref ) => {
          swayMap.foreach { swayMap =>
            swayMap.get( key = teamId ).foreach { record =>
              ref ! record
            }
          }
          Behavior.same
        }

        case RemoveToken( teamId: String, userId: String ) => {
          swayMap.foreach { swayMap =>
            swayMap
              .get( key = teamId )
              .foreach( _.foreach { record =>
                swayMap.put(
                  teamId,
                  record.copy(
                    tokens = record.tokens.filterNot( _.userId == userId )
                  )
                )
              } )
          }
          Behavior.same
        }

        case Close() => {
          Behavior.stopped
        }
      }
    }

  }
}
