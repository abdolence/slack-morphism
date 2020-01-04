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

package org.latestbit.slack.morphism.examples.akka.templates

import java.time.Instant

import org.latestbit.slack.morphism.client.templating.SlackBlocksTemplate
import org.latestbit.slack.morphism.messages.SlackBlock

case class SlackHomeNewsItem( title: String, body: String, published: Instant )

class SlackHomeTabBlocksTemplateExample( latestNews: List[SlackHomeNewsItem], userId: String )
    extends SlackBlocksTemplate {

  override def renderBlocks(): List[SlackBlock] =
    blocks(
      blocks(
        sectionBlock(
          text = md"Hey ${formatSlackUserId( userId )}"
        ),
        dividerBlock(),
        contextBlock(
          blockElements(
            md"This is an example of Slack Home Tab",
            md"Last updated: ${formatDate( Instant.now() )}"
          )
        ),
        dividerBlock(),
        imageBlock( image_url = "https://www.gstatic.com/webp/gallery/4.png", alt_text = "Test Image" ),
        actionsBlock(
          blockElements(
            button( text = plain"Simple", action_id = "simple-home-button" )
          )
        ),
        blocks(
          )
      ),
      optBlocks( latestNews.nonEmpty )(
        sectionBlock(
          text = md"*LatestNews*"
        ),
        dividerBlock(),
        latestNews.map { news =>
          sectionBlock(
            text = md" * ${news.title}\n${formatSlackQuoteText( news.body )}"
          )
        }
      )
    )
}
