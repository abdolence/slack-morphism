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

package org.latestbit.slack.morphism.examples.http4s.db

import cats.effect.{ Bracket, ConcurrentEffect, LiftIO, Resource }
import swaydb.data.slice.Slice
import cats.implicits._
import cats.effect.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.latestbit.slack.morphism.examples.http4s.config.AppConfig
import cats.effect._
import cats.effect.IO
import org.latestbit.slack.morphism.common.{ SlackAccessTokenValue, SlackTeamId, SlackUserId }
import swaydb.{ IO => _, Set => _, _ }
import swaydb.serializers.Default._
import swaydb.cats.effect.Tag._
import swaydb.serializers.Serializer

import scala.concurrent.ExecutionContext.Implicits.global

class SlackTokensDb[F[_] : ConcurrentEffect]( storage: SlackTokensDb.SwayDbType ) extends StrictLogging {
  import SlackTokensDb._

  def insertToken( teamId: SlackTeamId, tokenRecord: TokenRecord ): F[Unit] = {
    LiftIO[F].liftIO(
      storage
        .get( key = teamId )
        .map(
          _.map( rec =>
            rec.copy(tokens =
              rec.tokens.filterNot( _.userId == tokenRecord.userId ) :+ tokenRecord
            )
          ).getOrElse(
            TeamTokensRecord(
              teamId = teamId,
              tokens = List(
                tokenRecord
              )
            )
          )
        )
        .flatMap { record =>
          storage.put( teamId, record ).map { _ =>
            logger.info( s"Inserting record for : ${teamId}/${tokenRecord.userId}" )
          }
        }
    )
  }

  def removeTokens( teamId: SlackTeamId, users: Set[SlackUserId] ): F[Unit] = {
    LiftIO[F].liftIO[Unit](
      storage
        .get( key = teamId )
        .flatMap( res =>
          res
            .map { record =>
              storage
                .put(
                  teamId,
                  record.copy(
                    tokens = record.tokens.filterNot( token => users.contains( token.userId ) )
                  )
                )
                .map( _ => logger.info( s"Removed tokens for: ${users.mkString( "," )}" ) )
            }
            .getOrElse( IO.unit )
        )
    )
  }

  def readTokens( teamId: SlackTeamId ): F[Option[TeamTokensRecord]] = {
    LiftIO[F].liftIO(
      storage.get( key = teamId )
    )
  }

  private def close(): F[Unit] = LiftIO[F].liftIO {
    IO( logger.info( s"Closing tokens database" ) ).flatMap( _ => storage.close() )
  }

}

object SlackTokensDb extends StrictLogging {
  case class TokenRecord( tokenType: String, tokenValue: SlackAccessTokenValue, userId: SlackUserId, scope: String )
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

  private type FunctionType = PureFunction[SlackTeamId, TeamTokensRecord, Apply.Map[TeamTokensRecord]]
  private type SwayDbType = swaydb.Map[SlackTeamId, TeamTokensRecord, FunctionType, IO]

  private def openDb[F[_] : ConcurrentEffect]( config: AppConfig ): F[SlackTokensDb[F]] = {
    implicit val cs = IO.contextShift( global )

    implicit val teamIdSerializer = new Serializer[SlackTeamId] {
      override def write( data: SlackTeamId ): Slice[Byte] = StringSerializer.write( data.value )

      override def read( data: Slice[Byte] ): SlackTeamId = SlackTeamId( StringSerializer.read( data ) )
    }

    implicitly[ConcurrentEffect[F]]
      .delay {
        logger.info( s"Opening database in dir: '${config.databaseDir}''" )
        persistent.Map[SlackTeamId, TeamTokensRecord, FunctionType, IO]( dir = config.databaseDir ).get
      }
      .map( storage => new SlackTokensDb[F]( storage ) )
  }

  def open[F[_] : ConcurrentEffect]( config: AppConfig ): Resource[F, SlackTokensDb[F]] = {
    Resource.make[F, SlackTokensDb[F]]( openDb[F]( config ) )( _.close() )
  }
}
