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

package org.latestbit.slack.morphism.coding

import java.time.Instant

import org.latestbit.slack.morphism.messages._
import org.latestbit.slack.morphism.client.templating.{
  SlackBlocksTemplate,
  SlackBlocksTemplateDsl,
  SlackMessageTemplate
}
import org.latestbit.slack.morphism.common.SlackActionId
import org.scalatest.flatspec.AnyFlatSpec

class SlackMessageTemplatingTestSuite extends AnyFlatSpec {

  "SlackMessageTemplate" should "be available to create simple instance" in {
    new SlackMessageTemplate {
      override def renderPlainText(): String = "Test template"
    }
  }

  it should "provide DSL to build blocks API" in {
    val testCond = 0

    new SlackMessageTemplate {

      override def renderPlainText(): String = "Test template"

      override def renderBlocks(): Option[List[SlackBlock]] =
        blocks(
          blocks(
            blocks(
              blocks(
                dividerBlock(),
                headerBlock( text = pt"Header" ),
                sectionBlock( text = md"Test: ${testCond}" ),
                optionally( testCond > 0 )( dividerBlock() ),
                sectionBlock(
                  text = md"Test",
                  fields = sectionFields(
                    md"Test 1",
                    md"Test 2",
                    pt"Test 3",
                    optionally( testCond > 0 )( pt"Test 3" )
                  ),
                  accessory = image( "https://example.net/image.png", alt_text = "test image" )
                ),
                actionsBlock(
                  elements = blockElements(
                    button( text = pt"test button 1", action_id = SlackActionId( "-" ) ),
                    button( text = pt"test button 2", action_id = SlackActionId( "-" ) ),
                    button(
                      text = pt"test button 3",
                      action_id = SlackActionId( "-" ),
                      confirm =
                        confirm( title = pt"confirm dialog title", text = md"Hey", confirm = pt"Ok", deny = pt"Cancel" )
                    ),
                    optionally( testCond > 0 )( button( text = pt"test button", action_id = SlackActionId( "-" ) ) )
                  )
                ),
                sectionBlock(
                  text = md"Test 2",
                  accessory = overflow(
                    action_id = SlackActionId( "-" ),
                    options = choiceItems(
                      choiceItem( text = pt"test-menu-item", value = "" )
                    )
                  )
                ),
                sectionBlock(
                  text = md"Test Radio Buttons",
                  accessory = radioButtons(
                    action_id = SlackActionId( "-" ),
                    options = choiceItems(
                      choiceItem( text = md"test-menu-item1", value = "" ),
                      choiceItem( text = pt"test-menu-item2", value = "" )
                    )
                  )
                ),
                sectionBlock(
                  text = md"Test Checkboxes",
                  accessory = checkboxes(
                    action_id = SlackActionId( "-" ),
                    options = choiceItems(
                      choiceItem( text = md"test-menu-item1", value = "" ),
                      choiceItem( text = pt"test-menu-item2", value = "" )
                    )
                  )
                ),
                inputBlock(
                  label = pt"Input",
                  element = staticSelect(
                    placeholder = pt"test",
                    action_id = SlackActionId( "-" ),
                    options = choiceItems(
                      choiceItem( text = pt"test-menu-item", value = "" ),
                      optionally( testCond > 0 )( choiceItem( text = pt"test-menu-item2", value = "" ) )
                    ),
                    confirm = confirm(
                      title = pt"Test title",
                      text = md"Confirm this",
                      confirm = pt"OK",
                      deny = pt"Cancel"
                    )
                  )
                ),
                inputBlock(
                  label = pt"Input",
                  element = multiUsersSelect(
                    placeholder = pt"test",
                    action_id = SlackActionId( "-" ),
                    initial_users = choiceStrItems(
                      "test-user-1"
                    )
                  )
                ),
                contextBlock(
                  elements = blockElements(
                    pt"Test",
                    image( "https://example.net/image.png", alt_text = "test image" )
                  )
                )
              )
            ),
            blocks(
              blocks(
                dividerBlock()
              ),
              blocks(
                sectionBlock(
                  text = md"A message *with some bold text* and _some italicized text_."
                )
              ),
              blocks(
                sectionBlock(
                  text =
                    md"${formatUrl( "https://example.com", "Overlook Hotel" )}\n :star: \n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s."
                ),
                sectionBlock(
                  fields = sectionFields(
                    md"*Average Rating*\n1.0",
                    md"*Updated*\n${formatDate( Instant.now() )}",
                    optionally( testCond > 0 )( md"*Rating:* cool" )
                  )
                ),
                contextBlock(
                  blockElements(
                    md"*Author:* T. M. Schwartz"
                  )
                )
              )
            )
          )
        )
    }

  }

  it should "support text block interpolators" in {
    object TestInters extends SlackBlocksTemplateDsl {
      val m1: SlackBlockMarkDownText = md"test"
      val p1: SlackBlockPlainText    = pt"test"

      val testParamStr                = "1"
      val testParamInt                = 1
      val m1p: SlackBlockMarkDownText = md"test${testParamStr}${testParamInt}"
      val p1p: SlackBlockPlainText    = pt"test${testParamStr}${testParamInt}"
    }

    assert( TestInters.m1.text === "test" )
    assert( TestInters.p1.text === "test" )
    assert( TestInters.m1p.text === "test11" )
    assert( TestInters.p1p.text === "test11" )
  }

}
