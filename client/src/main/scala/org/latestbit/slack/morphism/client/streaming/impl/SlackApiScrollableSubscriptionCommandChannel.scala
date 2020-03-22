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

package org.latestbit.slack.morphism.client.streaming.impl

import java.util.concurrent.locks.ReentrantLock

import cats.Monad
import cats.implicits._
import cats.effect._
import cats.effect.concurrent.MVar
import org.latestbit.slack.morphism.client.SlackApiClientError
import org.latestbit.slack.morphism.client.streaming.{ SlackApiResponseScroller, SlackApiScrollableResponse }
import org.latestbit.slack.morphism.concurrent.{ AsyncSeqIterator, UniqueLockMonitor }
import org.reactivestreams.Subscriber

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Using

class SlackApiScrollableSubscriptionCommandChannel[F[_] : Monad, IT, PT, SR <: SlackApiScrollableResponse[IT, PT]](
    subscriber: Subscriber[_ >: IT],
    scrollableResponse: SlackApiResponseScroller[F, IT, PT, SR],
    maxItems: Option[Long] = None
)(
    implicit ec: ExecutionContext,
    ctxshift: ContextShift[IO]
) {
  import SlackApiScrollableSubscriptionCommandChannel._

  @volatile private var commandBuffer: Vector[Command] = Vector()
  @volatile private var notifyAsyncCommandCb: () => Unit = _
  private val statusLock = new ReentrantLock()

  private def consumerTask( channel: CommandChannel, currentState: ConsumerState[F, IT, PT, SR] ): IO[Unit] = {
    channel.take.flatMap {
      case RequestElements( n ) => {
        for {
          updatedState <- pumpNextBatchAsync( n, currentState )
          _ <- consumerTask( channel, updatedState )
        } yield ()
      }
      case Close => {
        IO.unit
      }
    }
  }

  private def producerCommandLoop( channel: CommandChannel ): IO[Unit] = {
    for {
      command <- takeNewCommand()
      producer = channel.put( command )
      fp <- producer.start
      _ <- fp.join
      _ <- (
            command match {
              case Close => IO.unit
              case _     => producerCommandLoop( channel )
            }
          )
    } yield ()
  }

  def takeNewCommand(): IO[Command] = {
    Async[IO].async[Command] { cb =>
      val _ = Using( UniqueLockMonitor.lockAndMonitor( statusLock ) ) { monitor =>
        if (commandBuffer.isEmpty) {
          notifyAsyncCommandCb = () => {
            val _ = Using( UniqueLockMonitor.lockAndMonitor( statusLock ) ) { monitorInAsyncCb =>
              val cmd = commandBuffer.head
              commandBuffer = commandBuffer.drop( 1 )
              notifyAsyncCommandCb = null
              monitorInAsyncCb.unlock()
              cb( cmd.asRight )
            }
          }
        } else {
          val cmd = commandBuffer.head
          commandBuffer = commandBuffer.drop( 1 )
          notifyAsyncCommandCb = null
          monitor.unlock()
          cb( cmd.asRight )
        }
      }
    }
  }

  private def pullBatchState( state: ConsumerState[F, IT, PT, SR] ): F[ConsumerState[F, IT, PT, SR]] = {
    if (state.remainItems.nonEmpty) {
      Monad[F].pure( state )
    } else {
      state.batchIterator
        .map { batchFuture =>
          val result: F[ConsumerState[F, IT, PT, SR]] =
            batchFuture.value().flatMap {
              case Right( items ) => {
                batchFuture.next().map { nextBatchFuture =>
                  state.copy(
                    remainItems = items,
                    batchIterator = nextBatchFuture
                  )
                }
              }
              case Left( err ) => {
                Monad[F].pure(
                  state.copy(
                    lastError = Some( err ),
                    batchIterator = None
                  )
                )
              }
            }
          result
        }
        .getOrElse(
          Monad[F].pure(
            state
          )
        )
    }
  }

  private def pumpNextBatchAsync(
      reqN: Long,
      state: ConsumerState[F, IT, PT, SR]
  ): IO[ConsumerState[F, IT, PT, SR]] = {
    Async[IO].async[ConsumerState[F, IT, PT, SR]] { asyncCallback =>
      if (!state.finished)
        pumpNextBatch( reqN, state, asyncCallback )
      else
        asyncCallback( state.asRight )
    }
  }

  private def pumpNextBatch(
      reqN: Long,
      state: ConsumerState[F, IT, PT, SR],
      asyncCallback: ( Either[Throwable, ConsumerState[F, IT, PT, SR]] ) => Unit
  ): Unit = {
    def finishState( state: ConsumerState[F, IT, PT, SR] ): Unit = {
      asyncCallback(
        Right(
          state.copy( finished = true )
        )
      )
    }
    val _ = pullBatchState( state ).map { state =>
      val n = maxItems.filter( _ < state.sent + reqN ).map( _ - state.sent ).getOrElse( reqN )
      if (n > 0) {
        state.lastError match {
          case Some( err ) => {
            subscriber.onError( err )
            finishState( state )
          }
          case _ => {
            if (state.remainItems.isEmpty) {
              subscriber.onComplete()
              finishState( state )
            } else {
              val ( toSend, toBuffer ) = state.remainItems.splitAt( n.toInt )
              toSend.foreach { item => subscriber.onNext( item ) }
              val updatedState: ConsumerState[F, IT, PT, SR] = state
                .copy(
                  sent = state.sent + toSend.size,
                  remainItems = toBuffer
                )

              if (toSend.size < n)
                pumpNextBatch( n - toSend.size, updatedState, asyncCallback )
              else {
                maxItems match {
                  case Some( maxItemsValue ) if maxItemsValue <= updatedState.sent => {
                    subscriber.onComplete()
                    finishState( updatedState )
                  }
                  case _ => {
                    asyncCallback(
                      updatedState.asRight
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        subscriber.onComplete()
        finishState( state )
      }
      ()
    }
  }

  def enqueue( command: Command ) = {
    val _ = Using( UniqueLockMonitor.lockAndMonitor( statusLock ) ) { monitor =>
      commandBuffer = commandBuffer :+ command
      monitor.unlock()
      Option( notifyAsyncCommandCb ).foreach( _.apply() )
    }
  }

  def start(): Unit = {
    (for {
      channel <- MVar[IO].empty[Command]
      consumer = consumerTask( channel, ConsumerState( Some( scrollableResponse.toAsyncScroller() ) ) )
      fc <- consumer.start
      _ <- producerCommandLoop( channel )
      _ <- fc.join
    } yield ()).unsafeRunAsyncAndForget()
  }

  def shutdown(): Unit = {
    Using( UniqueLockMonitor.lockAndMonitor( statusLock ) ) { _ => commandBuffer = Vector() }
    enqueue( Close )
  }
}

object SlackApiScrollableSubscriptionCommandChannel {

  sealed trait Command
  case class RequestElements( n: Long ) extends Command
  case object Close extends Command

  type CommandChannel = MVar[IO, Command]

  case class ConsumerState[F[_] : Monad, IT, PT, SR <: SlackApiScrollableResponse[IT, PT]](
      batchIterator: Option[AsyncSeqIterator[
        F,
        Either[SlackApiClientError, SR],
        Either[SlackApiClientError, Iterable[IT]]
      ]] = None,
      lastError: Option[Throwable] = None,
      remainItems: Iterable[IT] = List(),
      sent: Long = 0,
      finished: Boolean = false
  )
}
