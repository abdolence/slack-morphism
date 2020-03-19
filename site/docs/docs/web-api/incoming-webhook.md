---
layout: edocs
title: Post Incoming Webhook messages
permalink: docs/web-api/incoming-webhook
---
# Post Incoming Webhook messages

You can use `SlackApiClient.chat.postWebhookMessage()` to post [Slack Incoming Webhook](https://api.slack.com/messaging/webhooks) messages:

```
client.chat
    .postWebhookMessage(
        url = "https://hooks.slack.com/services/...", 
        req = SlackApiPostWebHookRequest(
          text = template.renderPlainText(),
          blocks = template.renderBlocks()
        )
     )
```
This method available to use without API tokens.
The Webhook URL could be gather either from OAuth responses or from your Slack app profile configuration.
