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

import org.latestbit.slack.morphism.client.reqresp.conversations.SlackApiConversationsHistoryResponse
import org.latestbit.slack.morphism.codecs.CirceCodecs
import org.latestbit.slack.morphism.common.SlackApiResponseMetadata
import org.latestbit.slack.morphism.events.{
  SlackChannelJoinMessage,
  SlackChannelNameMessage,
  SlackChannelPurposeMessage,
  SlackChannelTopicMessage
}
import org.scalatest.flatspec.AnyFlatSpec
import io.circe.parser._

class SlackApiRawJsonDecoderTestsSuite extends AnyFlatSpec with CirceCodecs {

  "Circe codecs" should "be able to decode a channel history with channel_joined, channel_topic, channel_purpose, channel_name messages" in {

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
          ts = "1583334580.006700",
          text = Some( "<@UID> has joined the channel" ),
          user = "UID",
          inviter = Some( "OTHERID" )
        ),
        SlackChannelPurposeMessage(
          ts = "1584535359.001300",
          text = Some( "<@UID> set the channel purpose: purpose-text" ),
          user = "UID",
          purpose = Some( "purpose-text" )
        ),
        SlackChannelTopicMessage(
          ts = "1584535194.000900",
          text = Some( "<@UID> set the channel topic: topic-text" ),
          user = "UID",
          topic = Some( "topic-text" )
        ),
        SlackChannelNameMessage(
          ts = "1584537248.001900",
          text = Some( "<@UID> has renamed the channel from \"test\" to \"test2\"" ),
          user = "UID",
          old_name = Some( "test" ),
          name = "test2"
        )
      ),
      has_more = Some( true ),
      pin_count = Some( 0 ),
      response_metadata = Some( SlackApiResponseMetadata( Some( "bmV4dF90czoxNTgyOTA3MjU3MDA0NjAw" ), None, None ) )
    )

    val result = decode[SlackApiConversationsHistoryResponse]( jsonResponse )

    assert( result === Right( expectedResponse ) )
  }

}
