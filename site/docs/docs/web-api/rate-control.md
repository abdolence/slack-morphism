---
layout: edocs
title: Rate Control, throttling and retrying
permalink: docs/web-api/rate-control
---
# Rate control, throttling and retrying Slack API method requests

## Enable rate control
Slack API defines [rate limits](https://api.slack.com/docs/rate-limits) to which all of your applications must follow.

Slack Morphism, starting from v1.1, provides an ability to throttling your requests to control the rate limits, 
and delay your calls when necessary.

By default, throttler *isn't* enabled, so you should enable it explicitly:
```
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._

import cats.instances.future._

// Creating a client instance with throttling
val client = 
    SlackApiClient
        .build
        .withThrottler( SlackApiRateThrottler.createStandardThrottler() )
        .create()
```
The example above creates a Slack API Client that follows the official rate limits from Slack.
Because the Slack rate limits apply per workspaces (separately), 
to use throttling and limits properly you *have to specify* workspace/team id in tokens:

```
// A token should have a workspace id
implicit val slackApiToken: SlackApiToken = SlackApiBotToken(
    "xoxb-89.....",
    workspaceId = Some( "TS......." ) 
)
``` 
There are also different rate tiers, and all Web API methods in Slack Morphism client 
are marked accordingly and follow those rate tiers limits.

## Rate control params
You can also customise rate control params using `SlackApiRateThrottler.createStandardThrottler( params = ...)`.

For example, you can set a global rate limit additionally to Slack limits using API like this:
```
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import scala.concurrent.duration._

import cats.instances.future._

// Defining params based on standard Slack limits
val params = SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS.copy(
      globalMaxRateLimit = Some(
         (100, 1.minute) // 100 requests per minute
      )
)

// Creating a client instance with throttling
val client = SlackApiClient
        .build
        .withThrottler( SlackApiRateThrottler.createStandardThrottler(params) )
        .create()

```

## Enable automatic retry for rate exceeded requests

To enable automatic retry of Slack Web API method requests, 
you need to specify `maxRetries` in rate control params (default value is 0):

```
val params = SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS.copy(
      maxRetries = 3 // retry maximum 3 times
)
```

The throttler implementation takes into account the timeout specified 
in an HTTP header (the `Retry-After` header) in Slack API rate limit response 
to delay your request at least that value.

Using rate control parameters, you can also enable automatic retrying for other errors additionally to `SlackApiRateLimitedError`:

```
val params =  SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS.copy(
    maxRetries = 3 // to retry maximum 3 times,
    retryFor = Set( classOf[SlackApiRateLimitedError], classOf[SlackApiConnectionError] )
)

```
