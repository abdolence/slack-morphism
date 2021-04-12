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

package org.latestbit.slack.morphism.common

/**
 * https://api.slack.com/types/file#file_types
 */
case class SlackFileType(value: String ) extends AnyVal

/**
 * Look complete list at https://api.slack.com/types/file#file_types
 */
object SlackFileType {
  final val Auto = SlackFileType("auto")
  final val Text = SlackFileType("text")
  final val Pdf = SlackFileType("pdf")
  final val Zip = SlackFileType("zip")
}