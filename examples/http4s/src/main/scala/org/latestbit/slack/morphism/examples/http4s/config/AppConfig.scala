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

package org.latestbit.slack.morphism.examples.http4s.config

/**
 * Example config
 *
 * @param httpServerHost listen http host
 * @param httpServerPort listen http port
 * @param slackAppConfig slack app config
 */
case class AppConfig(
    httpServerHost: String = "0.0.0.0",
    httpServerPort: Int = 8080,
    slackAppConfig: SlackAppConfig,
    databaseDir: String = "data"
)
