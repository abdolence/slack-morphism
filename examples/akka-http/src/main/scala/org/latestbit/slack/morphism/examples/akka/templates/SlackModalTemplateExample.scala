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

import org.latestbit.slack.morphism.client.templating.SlackModalViewTemplate
import org.latestbit.slack.morphism.messages._

class SlackModalTemplateExample() extends SlackModalViewTemplate {

  override def titleText(): SlackBlockPlainText = plain"Test Modal"

  override def submitText(): Option[SlackBlockPlainText] = Some( plain"Submit" )
  override def closeText(): Option[SlackBlockPlainText] = Some( plain"Cancel" )

  override def renderBlocks(): List[SlackBlock] =
    blocks(
      sectionBlock(
        text = md"Just a dummy window here, sorry",
        accessory = multiStaticMenu(
          placeholder = plain"With a dummy menu",
          action_id = "-",
          options = choiceItems(
            choiceItem( text = plain"First Option", value = "1" ),
            choiceItem( text = plain"Second Option", value = "2" ),
            choiceItem( text = plain"Third Option", value = "3" )
          )
        )
      ),
      inputBlock(
        label = plain"Dummy radio",
        element = radioButtons(
          action_id = "-",
          options = choiceItems(
            choiceItem( text = plain"Radio 1", value = "1" ),
            choiceItem( text = plain"Radio 2", value = "2" ),
            choiceItem( text = plain"Radio 3", value = "3" )
          )
        )
      )
    )

}
