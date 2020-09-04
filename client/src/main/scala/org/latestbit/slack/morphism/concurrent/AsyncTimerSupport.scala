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

import scala.languageFeature.implicitConversions
import java.util.concurrent.{ ScheduledExecutorService, TimeUnit }

import cats.effect._
import cats.syntax.all._

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

/**
 * Auxiliary interface to support delayed effects for different kind of monads
 * @tparam F effect kind (Future, IO)
 */
trait AsyncTimerSupport[F[_]] {

  /**
   * Schedule an effect
   * @param effect a lazily evaluated effect
   * @param duration delay duration
   * @param scheduledExecutor scheduler
   * @param ec execution context
   * @tparam A delayed result type
   * @return an effect in the specified monad
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

  class IOAsyncTimerSupport[F[_] : ConcurrentEffect]() extends AsyncTimerSupport[F] {

    override def delayed[A](
        effect: () => F[A],
        duration: FiniteDuration,
        scheduledExecutor: ScheduledExecutorService
    )( implicit
        ec: ExecutionContext
    ): F[A] = {
      implicit val timer = IO.timer( ec, scheduledExecutor )
      implicit val cs    = IO.contextShift( ec )

      LiftIO[F].liftIO( IO.sleep( duration ).start ).flatMap { _ => effect() }

    }
  }

  implicit def ioToAsyncTimerSupport[F[_] : ConcurrentEffect]: AsyncTimerSupport[F] = new IOAsyncTimerSupport()

}
