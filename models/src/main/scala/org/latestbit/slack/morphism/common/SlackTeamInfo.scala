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

package org.latestbit.slack.morphism.common

/**
 * Slack Team info
 */
case class SlackTeamInfo(
    id: String,
    name: Option[String] = None,
    domain: Option[String] = None,
    email_domain: Option[String] = None,
    enterprise_id: Option[String] = None,
    enterprise_name: Option[String] = None,
    icon: Option[SlackIcon] = None
)

/**
 * Basic Slack team information
 */
case class SlackBasicTeamInfo( id: String, name: Option[String] = None )
