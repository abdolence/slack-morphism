package org.latestbit.slack.morphism.client.impl

import org.latestbit.slack.morphism.client.SlackApiClientBackend.SttpFutureBackendType
import org.latestbit.slack.morphism.client.reqresp.conversations.SlackApiConversationsHistoryResponse
import org.latestbit.slack.morphism.codecs.implicits._
import org.latestbit.slack.morphism.common.SlackApiResponseMetadata
import org.latestbit.slack.morphism.events.SlackJoinedChannelMessage
import org.scalatest.flatspec.AnyFlatSpec
import sttp.client.Response
import sttp.client.testing.SttpBackendStub
import sttp.model.{Header, MediaType, StatusCode, Uri}


class SlackApiHttpProtocolSupportSuite extends AnyFlatSpec with SlackApiHttpProtocolSupport {

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
      |    }
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

  "The SlackApiHttpProtocolSupport" should "be able to decode an example channel_joined message" in {
    val testResponse: Response[Either[String, String]] = Response(
      Right(jsonResponse),
      StatusCode.notValidated(200),
      "OK",
      Seq(Header.contentType(MediaType.ApplicationJson)),
      List()
    )

    val result = decodeSlackResponse[SlackApiConversationsHistoryResponse](Uri.notValidated("test"), testResponse)

    val expectedResponse = SlackApiConversationsHistoryResponse(
      List(SlackJoinedChannelMessage("1583334580.006700", None, None, None, None, None, None, None, Some("<@UID> has joined the channel"), None, "UID", Some("OTHERID"))),
      Some(true),
      Some(0),
      Some(SlackApiResponseMetadata(Some("bmV4dF90czoxNTgyOTA3MjU3MDA0NjAw"), None, None))
    )
    
    assert(result === Right(expectedResponse))
  }

  override protected implicit val sttpBackend: SttpFutureBackendType = SttpBackendStub.asynchronousFuture
}
