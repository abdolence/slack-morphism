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

package org.latestbit.slack.morphism.ext

import sttp.model.Uri
import scala.language.implicitConversions

object SttpExt {

  implicit class UriExtensions( val uri: Uri ) extends AnyVal {

    /**
     * Adds the given parameter with an optional value to the query if it is present.
     */
    def param( k: String, v: Option[String] ): Uri = v.map( uri.param( k, _ ) ).getOrElse( uri )
  }

}
