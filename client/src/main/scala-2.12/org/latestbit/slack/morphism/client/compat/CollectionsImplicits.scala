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

package org.latestbit.slack.morphism.client.compat

import scala.language.implicitConversions

object CollectionsImplicits {

  implicit class IterableExtensions[A]( val iterable: TraversableOnce[A] ) extends AnyVal {

    def maxOption[B >: A]( implicit ord: Ordering[B] ): Option[A] = {
      if (iterable.isEmpty)
        None
      else
        Some( iterable.max( ord ) )
    }
  }

}
