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

import org.latestbit.slack.morphism.messages._
import org.latestbit.slack.morphism.client.templating.{
  SlackBlocksTemplate,
  SlackBlocksTemplateDsl,
  SlackMessageTemplate
}
import org.scalatest.flatspec.AnyFlatSpec

class SlackMessageTemplatingTestSuite extends AnyFlatSpec {

  "SlackMessageTemplate" should "be available to create simple instance" in {
    new SlackMessageTemplate {
      override def renderPlainText(): String = "Test template"
    }
  }

  "it" should "provide DSL to build blocks API" in {
    val testCond = 0

    new SlackMessageTemplate {

      override def renderPlainText(): String = "Test template"

      override def renderBlocks(): Option[List[SlackBlock]] =
        blocks(
          blocks(
            blocks(
              blocks(
                dividerBlock(),
                sectionBlock( text = md"Test: ${testCond}" ),
                optBlock( testCond > 0 )( dividerBlock() ),
                sectionBlock(
                  text = md"Test",
                  fields = sectionFields(
                    md"Test 1",
                    md"Test 2",
                    plain"Test 3",
                    optSectionField( testCond > 0 )( plain"Test 3" )
                  ),
                  accessory = image( "https://example.net/image.png" )
                ),
                contextBlock(
                  elements = blockElements(
                    button( text = plain"test button", action_id = "-" ),
                    button( text = plain"test button", action_id = "-" )
                  )
                ),
                sectionBlock(
                  text = md"Test 2",
                  accessory = overflowMenu(
                    action_id = "-",
                    options = choiceItems(
                      choiceItem( text = plain"test-menu-item", value = "" )
                    )
                  )
                ),
                contextBlock(
                  elements = blockElements(
                    staticMenu(
                      placeholder = plain"test",
                      action_id = "-",
                      options = choiceItems(
                        choiceItem( text = plain"test-menu-item", value = "" ),
                        optChoiceItem( testCond > 0 )( choiceItem( text = plain"test-menu-item2", value = "" ) )
                      ),
                      confirm = confirm(
                        title = plain"Test title",
                        text = md"Confirm this",
                        confirm = plain"OK",
                        deny = plain"Cancel"
                      )
                    ),
                    multiUsersListMenu(
                      placeholder = plain"test",
                      action_id = "-",
                      initial_users = choiceStrItems(
                        "test-user-1"
                      )
                    )
                  )
                )
              )
            ),
            blocks(
              blocks(
                dividerBlock()
              )
            )
          )
        )
    }

  }

  it should "support text block interpolators" in {
    object TestInters extends SlackBlocksTemplateDsl {
      val m1: SlackBlockMarkDownText = md"test"
      val p1: SlackBlockPlainText = plain"test"

      val testParamStr = "1"
      val testParamInt = 1
      val m1p: SlackBlockMarkDownText = md"test${testParamStr}${testParamInt}"
      val p1p: SlackBlockPlainText = plain"test${testParamStr}${testParamInt}"
    }

    assert( TestInters.m1.text === "test" )
    assert( TestInters.p1.text === "test" )
    assert( TestInters.m1p.text === "test11" )
    assert( TestInters.p1p.text === "test11" )
  }

}
