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

package org.latestbit.slack.morphism.client.templating

import org.latestbit.slack.morphism.messages.SlackBlockPlainText
import org.latestbit.slack.morphism.views.SlackModalView

/**
 * A template to render Slack modal views with blocks
 */
trait SlackModalViewTemplate extends SlackBlocksTemplate {
  def titleText(): SlackBlockPlainText
  def submitText(): Option[SlackBlockPlainText] = None
  def closeText(): Option[SlackBlockPlainText] = None

  def toModalView() = {
    SlackModalView(
      title = titleText(),
      submit = submitText(),
      close = closeText(),
      blocks = renderBlocks()
    )
  }
}
