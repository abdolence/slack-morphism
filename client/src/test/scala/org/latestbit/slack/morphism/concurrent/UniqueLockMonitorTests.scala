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

import java.util.concurrent.locks.Lock

import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

import scala.util.Using

class UniqueLockMonitorTests extends AnyFunSuite with MockFactory {

  test( "create a monitor with a simple lock" ) {
    val lockMock = mock[Lock]
    ( lockMock.lock _ ).expects().once()
    UniqueLockMonitor.lockAndMonitor( lockMock )
  }

  test( "create a monitor and unlock manually" ) {
    val lockMock = mock[Lock]
    inSequence(
      ( lockMock.lock _ ).expects().once(),
      ( lockMock.unlock _ ).expects().once()
    )
    val monitor = UniqueLockMonitor.lockAndMonitor( lockMock )
    monitor.unlock()
  }

  test( "create a monitor and unlock automatically with try-with-resources" ) {
    val lockMock = mock[Lock]
    inSequence(
      ( lockMock.lock _ ).expects().once(),
      ( lockMock.unlock _ ).expects().once()
    )
    Using.resource( UniqueLockMonitor.lockAndMonitor( lockMock ) ) { monitor => assert( monitor != null ) }
  }

  test( "create a monitor and unlock manually with try-with-resources" ) {
    val lockMock = mock[Lock]
    ( lockMock.lock _ ).expects().once()
    ( lockMock.unlock _ ).expects().once()
    Using.resource( UniqueLockMonitor.lockAndMonitor( lockMock ) ) { monitor => monitor.unlock() }
  }

  test( "create a monitor and trying to lock/unlock manually many times fails" ) {
    val lockMock = mock[Lock]
    inSequence(
      ( lockMock.lock _ ).expects().once(),
      ( lockMock.unlock _ ).expects().once()
    )
    Using.resource( UniqueLockMonitor.lockAndMonitor( lockMock ) ) { monitor =>
      assertThrows[IllegalArgumentException] {
        monitor.lock()
      }
      monitor.unlock()
      assertThrows[IllegalArgumentException] {
        monitor.unlock()
      }
    }
  }

  test( "create a monitor and release it" ) {
    val lockMock = mock[Lock]
    ( lockMock.lock _ ).expects().once()
    Using.resource( UniqueLockMonitor.lockAndMonitor( lockMock ) ) { monitor =>
      monitor.release()
      assertThrows[IllegalArgumentException] {
        monitor.lock()
      }

      assertThrows[IllegalArgumentException] {
        monitor.unlock()
      }
    }
  }

  test( "create a monitor unlocked" ) {
    val lockMock = mock[Lock]
    UniqueLockMonitor.monitor( lockMock, isLocked = false )
  }

  test( "create a monitor locked already" ) {
    val lockMock = mock[Lock]
    ( lockMock.unlock _ ).expects().once()
    Using.resource( UniqueLockMonitor.monitor( lockMock, isLocked = true ) ) { _ => }
  }

}
