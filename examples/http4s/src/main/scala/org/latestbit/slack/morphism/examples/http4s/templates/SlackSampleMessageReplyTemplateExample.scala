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

package org.latestbit.slack.morphism.examples.http4s.templates

import java.time.Instant

import org.latestbit.slack.morphism.client.templating.{ SlackMessageTemplate, SlackTextFormatters }
import org.latestbit.slack.morphism.messages.SlackBlock

class SlackSampleMessageReplyTemplateExample( replyToMessage: String ) extends SlackMessageTemplate {

  override def renderPlainText(): String =
    s"I've just received from you some text:"

  override def renderBlocks(): Option[List[SlackBlock]] =
    blocks(
      sectionBlock(
        text = md"I've just received from you some text:\n${formatSlackQuoteText( replyToMessage )}"
      ),
      dividerBlock(),
      contextBlock(
        blockElements(
          md"I'm glad that you still remember me",
          md"Current time is: ${formatDate( Instant.now(), SlackTextFormatters.SlackDateTimeFormats.DATE_LONG_PRETTY )}"
        )
      ),
      actionsBlock(
        blockElements(
          button( text = pt"Simple", action_id = "simple-message-button" )
        )
      )
    )

}
