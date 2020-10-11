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

import org.latestbit.circe.adt.codec._

/**
 * Slack API token types.
 */
sealed trait SlackApiTokenType {
  val name: String
}

object SlackApiTokenType {

  @JsonAdt( "bot" )
  case object Bot extends SlackApiTokenType {
    override val name = "bot"
  }

  @JsonAdt( "user" )
  case object User extends SlackApiTokenType {
    override val name = "user"
  }

  @JsonAdt( "app" )
  case object App extends SlackApiTokenType {
    override val name = "app"
  }
}
