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

package org.latestbit.slack.morphism.concurrent

import cats.FlatMap

import java.util.concurrent.ScheduledExecutorService
import cats.effect._
import cats.implicits._

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration.FiniteDuration

/**
 * Auxiliary interface to support delayed effects for different kind of monads
 * @tparam F
 *   effect kind (Future, IO)
 */
trait AsyncTimerSupport[F[_]] {

  /**
   * Schedule an effect
   * @param effect
   *   a lazily evaluated effect
   * @param duration
   *   delay duration
   * @param scheduledExecutor
   *   scheduler
   * @param ec
   *   execution context
   * @tparam A
   *   delayed result type
   * @return
   *   an effect in the specified monad
   */
  def delayed[A]( effect: () => F[A], duration: FiniteDuration, scheduledExecutor: ScheduledExecutorService )( implicit
      ec: ExecutionContext
  ): F[A]

}

object AsyncTimerSupport {

  class FutureAsyncTimerSupport() extends AsyncTimerSupport[Future] {

    override def delayed[A](
        effect: () => Future[A],
        duration: FiniteDuration,
        scheduledExecutor: ScheduledExecutorService
    )( implicit
        ec: ExecutionContext
    ): Future[A] = {
      val promise = Promise[A]()
      scheduledExecutor.schedule(
        () => {
          promise.completeWith( effect() )
        },
        duration.length,
        duration.unit
      )
      promise.future
    }
  }

  implicit val futureToAsyncTimerSupport: AsyncTimerSupport[Future] = new FutureAsyncTimerSupport()

  class IOAsyncTimerSupport[F[_] : LiftIO : FlatMap]() extends AsyncTimerSupport[F] {

    override def delayed[A](
        effect: () => F[A],
        duration: FiniteDuration,
        scheduledExecutor: ScheduledExecutorService
    )( implicit
        ec: ExecutionContext
    ): F[A] = {
      val ec: ExecutionContext = ExecutionContext.fromExecutorService( scheduledExecutor )

      LiftIO[F].liftIO( IO.cede.evalOn( ec ).delayBy( duration ).startOn( ec ) ).flatMap { _ => effect() }

    }
  }

  implicit def ioToAsyncTimerSupport[F[_] : LiftIO : FlatMap]: AsyncTimerSupport[F] = new IOAsyncTimerSupport()

}
