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

import cats.effect.{ IO, Timer }

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

trait AsyncTimerSupport[F[_]] {

  def delayed[A]( effect: () => F[A], duration: FiniteDuration, scheduledExecutor: ScheduledExecutorService )(
      implicit ec: ExecutionContext
  ): F[A]
}

object AsyncTimerSupport {

  class FutureAsyncTimerSupport() extends AsyncTimerSupport[Future] {

    override def delayed[A](
        effect: () => Future[A],
        duration: FiniteDuration,
        scheduledExecutor: ScheduledExecutorService
    )(
        implicit ec: ExecutionContext
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

  class IOAsyncTimerSupport() extends AsyncTimerSupport[IO] {

    override def delayed[A](
        effect: () => IO[A],
        duration: FiniteDuration,
        scheduledExecutor: ScheduledExecutorService
    )(
        implicit ec: ExecutionContext
    ): IO[A] = {
      implicit val timer = IO.timer( ec, scheduledExecutor )
      IO.sleep( duration ).flatMap { _ => effect() }
    }
  }

  implicit val ioToAsyncTimerSupport: AsyncTimerSupport[IO] = new IOAsyncTimerSupport()

}
