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

/**
 * A stateful implementation of an auto-closable lock/monitor for the specified lock
 * with an ability to unlock it preliminarily.
 * The class philosophy and namings are borrowed from C++ std::unique_lock<>.
 *
 * Usage example:
 * {{{
 *
 * import scala.util.Using
 * import java.util.concurrent.locks.ReentrantLock
 *
 * class Test {
 *
 *   private val statusLock = new ReentrantLock()
 *
 *   def tryWithResourceExample() = {
 *      Using( UniqueLockMonitor.lockAndMonitor( statusLock ) ) { monitor =>
 *         // do something inside a lock
 *      }
 *   }
 *
 *   def preliminaryUnlockExample() = {
 *      Using( UniqueLockMonitor.lockAndMonitor( statusLock ) ) { monitor =>
 *          // do something inside a lock
 *          monitor.unlock()
 *          // do something else outside locking and UniqueLockMonitor.close() now does nothing
 *      }
 *   }
 *
 * }
 *
 * }}}
 *
 * @note this class isn't reentrant and isn't supposed to be shared across threads or used as a field.
 *       This class should be used as a stack/method local variable with try-with-resources.
 */
final class UniqueLockMonitor private ( private var monitorOnLock: Lock, private var isLocked: Boolean )
    extends AutoCloseable {

  /**
   * Lock manually.
   * Consequent multiple locks aren't allowed to avoid mistakes and sharing monitor instances.
   *
   * @return the same instance
   */
  def lock(): UniqueLockMonitor = {
    require( monitorOnLock != null, "Monitor has been released" )
    require( !isLocked, "Monitor has been already locked" )
    monitorOnLock.lock()
    isLocked = true
    this
  }

  /**
   * Unlock manually.
   * Consequent multiple unlocks aren't allowed to avoid mistakes and sharing monitor instances.
   *
   * @return the same instance
   */
  def unlock(): UniqueLockMonitor = {
    require( monitorOnLock != null, "Monitor has been released" )
    require( isLocked, "Monitor has been already unlocked" )
    monitorOnLock.unlock()
    isLocked = false
    this
  }

  /**
   * Release/take a lock from monitor, so it wouldn't be unlocked automatically in close.
   * You can't use lock/unlock after releasing a monitor.
   */
  def release(): Lock = {
    require( monitorOnLock != null, "Monitor has been already released" )
    isLocked = false
    val result = monitorOnLock
    monitorOnLock = null
    result
  }

  /**
   * Auto close (unlock) if it hasn't been released or unlocked already.
   */
  override def close(): Unit = {
    if (monitorOnLock != null && isLocked) {
      unlock()
    }
    ()
  }

}

object UniqueLockMonitor {

  /**
   * Lock and monitor the user specified lock resource.
   *
   * @param lock user lock implementation
   * @return monitor instance
   */
  def lockAndMonitor( lock: Lock ) = monitor( lock, isLocked = false ).lock()

  /**
   * Monitor the specified user lock.
   *
   * @param lock a user lock
   * @param isLocked current state of lock
   * @return monitor instance
   */
  def monitor( lock: Lock, isLocked: Boolean ): UniqueLockMonitor = new UniqueLockMonitor( lock, isLocked )
}
