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

package org.latestbit.slack.morphism.client.reqresp.dnd

import org.latestbit.slack.morphism.common._

/**
 * Request of https://api.slack.com/methods/dnd.info
 */
case class SlackApiDndInfoRequest( user: Option[SlackUserId] = None )

/**
 * Response of https://api.slack.com/methods/dnd.info
 */
case class SlackApiDndInfoResponse(
    dnd_enabled: Option[Boolean] = None,
    next_dnd_start_ts: Option[SlackDateTime] = None,
    next_dnd_end_ts: Option[SlackDateTime] = None,
    snooze_enabled: Option[Boolean] = None,
    snooze_endtime: Option[SlackDateTime] = None,
    snooze_remaining: Option[Long] = None
)
