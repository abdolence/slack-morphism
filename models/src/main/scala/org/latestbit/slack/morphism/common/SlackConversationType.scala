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

import org.latestbit.circe.adt.codec.JsonAdt

sealed trait SlackConversationType {
  val value: String
}

object SlackConversationType {

  @JsonAdt( IM.value )
  case object IM extends SlackConversationType {
    override final val value = "im"
  }

  @JsonAdt( MPIM.value )
  case object MPIM extends SlackConversationType {
    override final val value = "mpim"
  }

  @JsonAdt( PRIVATE.value )
  case object PRIVATE extends SlackConversationType {
    override final val value = "private"
  }

  @JsonAdt( PUBLIC.value )
  case object PUBLIC extends SlackConversationType {
    override final val value = "public"
  }
}
