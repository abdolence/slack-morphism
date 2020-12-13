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

package org.latestbit.slack.morphism.coding

import io.circe.DecodingFailure
import org.latestbit.slack.morphism.client.reqresp.conversations.SlackApiConversationsHistoryResponse
import org.latestbit.slack.morphism.codecs.{ CirceCodecs, SlackCirceJsonSettings }
import org.latestbit.slack.morphism.common._
import org.latestbit.slack.morphism.events._
import org.scalatest.flatspec.AnyFlatSpec
import io.circe.{ Decoder, Encoder, Json }
import io.circe.syntax._
import io.circe.parser._
import org.latestbit.slack.morphism.messages.{
  SlackBlockConversationListSelectElement,
  SlackListFilterConversationType
}
import org.latestbit.slack.morphism.views.SlackModalView

class SlackApiRawJsonDecoderTestsSuite extends AnyFlatSpec with CirceCodecs {

  "Circe codecs" should "be able to decode a channel history with different message subtypes" in {

    val jsonResponse =
      """
            |{
            |  "ok": true,
            |  "messages": [
            |    {
            |      "type": "message",
            |      "subtype": "channel_join",
            |      "ts": "1583334580.006700",
            |      "user": "UID",
            |      "text": "<@UID> has joined the channel",
            |      "inviter": "OTHERID"
            |    },
            |    {
            |            "type": "message",
            |            "subtype": "channel_purpose",
            |            "ts": "1584535359.001300",
            |            "user": "UID",
            |            "text": "<@UID> set the channel purpose: purpose-text",
            |            "purpose": "purpose-text"
            |        },
            |        {
            |            "type": "message",
            |            "subtype": "channel_topic",
            |            "ts": "1584535194.000900",
            |            "user": "UID",
            |            "text": "<@UID> set the channel topic: topic-text",
            |            "topic": "topic-text"
            |        },
            |        {
            |            "type": "message",
            |            "subtype": "channel_name",
            |            "ts": "1584537248.001900",
            |            "user": "UID",
            |            "text": "<@UID> has renamed the channel from \"test\" to \"test2\"",
            |            "old_name": "test",
            |            "name": "test2"
            |        },
            |        {
            |            "type": "message",
            |            "subtype": "bot_add",
            |            "ts": "1584537248.001900",
            |            "user": "UID",
            |            "text": "added an integration to this channel: <https://org.slack.com/services/bot-id|bot-name>",
            |            "bot_id": "bot-id",
            |            "bot_link": "<https://org.slack.com/services/bot-id|bot-name>"
            |        },
            |        {
            |            "type": "message",
            |            "subtype": "bot_remove",
            |            "ts": "1584537248.001900",
            |            "user": "UID",
            |            "text": "removed an integration from this channel: <https://org.slack.com/services/bot-id|bot-name>",
            |            "bot_id": "bot-id",
            |            "bot_link": "<https://org.slack.com/services/bot-id|bot-name>"
            |        },
            |        {
            |            "type": "message",
            |            "subtype": "channel_join",
            |            "ts": "1585252947.004300",
            |            "user": "UID1",
            |            "text": "<@UID> has joined the channel",
            |            "inviter": "UID2"
            |        }        
            |  ],
            |  "has_more": true,
            |  "pin_count": 0,
            |  "channel_actions_ts": null,
            |  "channel_actions_count": 0,
            |  "response_metadata": {
            |    "next_cursor": "bmV4dF90czoxNTgyOTA3MjU3MDA0NjAw"
            |  }
            |}
            |""".stripMargin

    val expectedResponse = SlackApiConversationsHistoryResponse(
      messages = List(
        SlackChannelJoinMessage(
          ts = SlackTs( "1583334580.006700" ),
          text = Some( "<@UID> has joined the channel" ),
          user = SlackUserId( "UID" ),
          inviter = Some( SlackUserId( "OTHERID" ) )
        ),
        SlackChannelPurposeMessage(
          ts = SlackTs( "1584535359.001300" ),
          text = Some( "<@UID> set the channel purpose: purpose-text" ),
          user = SlackUserId( "UID" ),
          purpose = Some( "purpose-text" )
        ),
        SlackChannelTopicMessage(
          ts = SlackTs( "1584535194.000900" ),
          text = Some( "<@UID> set the channel topic: topic-text" ),
          user = SlackUserId( "UID" ),
          topic = Some( "topic-text" )
        ),
        SlackChannelNameMessage(
          ts = SlackTs( "1584537248.001900" ),
          text = Some( "<@UID> has renamed the channel from \"test\" to \"test2\"" ),
          user = SlackUserId( "UID" ),
          old_name = Some( "test" ),
          name = "test2"
        ),
        SlackBotAddMessage(
          ts = SlackTs( "1584537248.001900" ),
          text = Some( "added an integration to this channel: <https://org.slack.com/services/bot-id|bot-name>" ),
          user = SlackUserId( "UID" ),
          bot_id = Some( SlackBotId( "bot-id" ) ),
          bot_link = Some( "<https://org.slack.com/services/bot-id|bot-name>" )
        ),
        SlackBotRemoveMessage(
          ts = SlackTs( "1584537248.001900" ),
          text = Some( "removed an integration from this channel: <https://org.slack.com/services/bot-id|bot-name>" ),
          user = SlackUserId( "UID" ),
          bot_id = Some( SlackBotId( "bot-id" ) ),
          bot_link = Some( "<https://org.slack.com/services/bot-id|bot-name>" )
        ),
        SlackChannelJoinMessage(
          ts = SlackTs( "1585252947.004300" ),
          text = Some( "<@UID> has joined the channel" ),
          user = SlackUserId( "UID1" ),
          inviter = Some( SlackUserId( "UID2" ) )
        )
      ),
      has_more = Some( true ),
      pin_count = Some( 0 ),
      response_metadata =
        Some( SlackApiResponseMetadata( Some( SlackCursorId( "bmV4dF90czoxNTgyOTA3MjU3MDA0NjAw" ) ), None, None ) )
    )

    val result = decode[SlackApiConversationsHistoryResponse]( jsonResponse )

    assert( result === Right( expectedResponse ) )
  }

  it should "decode messages with blocks and mrkdown radio buttons" in {
    val json =
      """
          | {
          |  "type": "modal",
          |  "title": {
          |    "type": "plain_text",
          |    "text": "My App",
          |    "emoji": true
          |  },
          |  "submit": {
          |    "type": "plain_text",
          |    "text": "Submit",
          |    "emoji": true
          |  },
          |  "close": {
          |    "type": "plain_text",
          |    "text": "Cancel",
          |    "emoji": true
          |  },
          |  "blocks": [
          |    {
          |      "type": "section",
          |      "text": {
          |        "type": "plain_text",
          |        "text": "Check out these rad radio buttons"
          |      },
          |      "accessory": {
          |        "type": "radio_buttons",
          |        "action_id": "this_is_an_action_id",
          |        "initial_option": {
          |          "value": "A1",
          |          "text": {
          |            "type": "plain_text",
          |            "text": "Radio 1"
          |          }
          |        },
          |        "options": [
          |          {
          |            "value": "A1",
          |            "text": {
          |              "type": "plain_text",
          |              "text": "Radio 1"
          |            }
          |          },
          |          {
          |            "value": "A2",
          |            "text": {
          |              "type": "mrkdwn",
          |              "text": "Radio 2"
          |            }
          |          }
          |        ]
          |      }
          |    },
          |    {
          |    "type": "section",
          |    "block_id": "section678",
          |    "text": {
          |      "type": "mrkdwn",
          |      "text": "Pick an item from the dropdown list"
          |    },
          |    "accessory": {
          |      "action_id": "text1234",
          |      "type": "static_select",
          |      "placeholder": {
          |        "type": "plain_text",
          |        "text": "Select an item"
          |      },
          |      "options": [
          |        {
          |          "text": {
          |            "type": "plain_text",
          |            "text": "*this is plain_text text*"
          |          },
          |          "value": "value-0"
          |        },
          |        {
          |          "text": {
          |            "type": "plain_text",
          |            "text": "*this is plain_text text*"
          |          },
          |          "value": "value-1"
          |        },
          |        {
          |          "text": {
          |            "type": "plain_text",
          |            "text": "*this is plain_text text*"
          |          },
          |          "value": "value-2"
          |        }
          |      ]
          |    }
          |  }
          |  ]
          |}
          |""".stripMargin

    decode[SlackModalView]( json ) match {
      case Right( modal ) => {
        assert( modal.blocks.nonEmpty )
      }
      case Left( err ) => fail( err )
    }
  }

  it should "not decode messages with blocks and mrkdown selects" in {
    val json =
      """
              | {
              |  "type": "modal",
              |  "title": {
              |    "type": "plain_text",
              |    "text": "My App",
              |    "emoji": true
              |  },
              |  "submit": {
              |    "type": "plain_text",
              |    "text": "Submit",
              |    "emoji": true
              |  },
              |  "close": {
              |    "type": "plain_text",
              |    "text": "Cancel",
              |    "emoji": true
              |  },
              |  "blocks": [
              |    {
              |    "type": "section",
              |    "block_id": "section678",
              |    "text": {
              |      "type": "mrkdwn",
              |      "text": "Pick an item from the dropdown list"
              |    },
              |    "accessory": {
              |      "action_id": "text1234",
              |      "type": "static_select",
              |      "placeholder": {
              |        "type": "plain_text",
              |        "text": "Select an item"
              |      },
              |      "options": [
              |        {
              |          "text": {
              |            "type": "mrkdwn",
              |            "text": "*this is plain_text text*"
              |          },
              |          "value": "value-0"
              |        }
              |      ]
              |    }
              |  }
              |  ]
              |}
              |""".stripMargin

    decode[SlackModalView]( json ) match {
      case Right( _ ) => {
        fail()
      }
      case Left( err ) => {
        assert( err.isInstanceOf[DecodingFailure] )
      }
    }
  }

  it should "decode messages with blocks and checkboxes" in {
    val json =
      """
              | {
              |    "type": "modal",
              |    "title": {
              |        "type": "plain_text",
              |        "text": "My App",
              |        "emoji": true
              |    },
              |    "submit": {
              |        "type": "plain_text",
              |        "text": "Submit",
              |        "emoji": true
              |    },
              |    "close": {
              |        "type": "plain_text",
              |        "text": "Cancel",
              |        "emoji": true
              |    },
              |    "blocks": [
              |        {
              |            "type": "section",
              |            "text": {
              |                "type": "plain_text",
              |                "text": "Check out these charming checkboxes"
              |            },
              |            "accessory": {
              |                "type": "checkboxes",
              |                "action_id": "this_is_an_action_id",
              |                "initial_options": [{
              |                    "value": "A1",
              |                    "text": {
              |                        "type": "plain_text",
              |                        "text": "Checkbox 1"
              |                    }
              |                }],
              |                "options": [
              |                    {
              |                        "value": "A1",
              |                        "text": {
              |                            "type": "plain_text",
              |                            "text": "Checkbox 1"
              |                        }
              |                    },
              |                    {
              |                        "value": "A2",
              |                        "text": {
              |                            "type": "mrkdwn",
              |                            "text": "Checkbox 2"
              |                        }
              |                    }
              |                ]
              |            }
              |        }
              |    ]
              |}
              |""".stripMargin

    decode[SlackModalView]( json ) match {
      case Right( modal ) => {
        assert( modal.blocks.nonEmpty )
      }
      case Left( err ) => fail( err )
    }
  }

  it should "encode and decode conversations_select filter" in {
    val json =
      """
          | {
          |  "type": "conversations_select",
          |  "action_id" : "-",
          |  "placeholder": {
          |    "type": "plain_text",
          |    "text": "Select a conversation",
          |    "emoji": true
          |  },
          |  "filter": {
          |    "include": [
          |      "public",
          |      "mpim"
          |    ],
          |    "exclude_bot_users" : true
          |  } 
          | }
          |""".stripMargin

    decode[SlackBlockConversationListSelectElement]( json ) match {
      case Right( convList ) => {
        convList.filter match {
          case Some( filter ) => {
            assert(
              filter.include.exists(
                _.toList === List( SlackListFilterConversationType.PUBLIC, SlackListFilterConversationType.MPIM )
              )
            )

            val encodedJson = convList.asJson.printWith( SlackCirceJsonSettings.printer )
            assert( encodedJson.contains( """"include":["public","mpim"]""" ) )
          }
          case _ => fail()
        }

      }
      case Left( err ) => fail( err )
    }
  }

}
