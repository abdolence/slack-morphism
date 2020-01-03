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

class SlackHomeTabBlocksTemplateExample( userId: String ) extends SlackBlocksTemplate {

  override def renderBlocks(): List[SlackBlock] =
    blocksGroup(
      blocks(
        block(
          sectionBlock(
            text = md"Hey ${formatSlackUserId( userId )}"
          )
        ),
        block( dividerBlock() ),
        block(
          contextBlock(
            blockElements(
              blockEl( md"Hello to this example tab" ),
              blockEl( md"Last updated: ${formatDate( Instant.now() )}" )
            )
          )
        ),
        block( dividerBlock() ),
        block( imageBlock( image_url = "https://www.gstatic.com/webp/gallery/4.png", alt_text = "Test Image" ) )
      )
    ).getOrElse( List() )
}
