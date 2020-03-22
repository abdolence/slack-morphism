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

import cats.effect.IO

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ Await, Future }
import scala.languageFeature.implicitConversions

trait SyncScrollerAwaiter[F[_]] {
  def await[A]( instance: F[A], duration: FiniteDuration ): A
}

object SyncScrollerAwaiter {

  class SyncFutureAwaiter extends SyncScrollerAwaiter[Future] {

    override def await[A]( instance: Future[A], duration: FiniteDuration ): A =
      Await.result( instance, duration )
  }

  implicit val futureToSyncFutureAwaiter: SyncScrollerAwaiter[Future] = new SyncFutureAwaiter()

  class SyncIOAwaiter extends SyncScrollerAwaiter[IO] {

    override def await[A]( instance: IO[A], duration: FiniteDuration ): A =
      Await.result(
        instance.unsafeToFuture(),
        duration
      )
  }

  implicit val ioToSyncFutureAwaiter: SyncScrollerAwaiter[IO] = new SyncIOAwaiter()

}
