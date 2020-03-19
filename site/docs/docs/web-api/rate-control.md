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

// Creating a client instance with throttling
val client = new SlackApiClient(
   throttler = SlackApiRateThrottler.createStandardThrottler()
)
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

## Customizing rate control params
You can also customise rate control params using `SlackApiRateThrottler.createStandardThrottler( params = ...)`.

For example, you can set a global rate limit additionally to Slack limits using API like this:
```
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._
import scala.concurrent.duration._

// Creating a client instance with throttling
val client = new SlackApiClient(
   throttler = SlackApiRateThrottler.createStandardThrottler(
        params = 
            SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS.copy(
                globalMaxRateLimit = Some(
                   (100, 1.minute) // 100 requests per minute
                )
            )
   )
)
```

## Enable automatic retry for rate exceeded requests

To enable automatic retry of Slack Web API method requests, 
you need to specify `maxRetries` in rate control params (default value is 0):

```
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._

// Creating a client instance with throttling
val client = new SlackApiClient(
   throttler = SlackApiRateThrottler.createStandardThrottler(
        params = 
            SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS.copy(
                maxRetries = 3 // retry maximum 3 times
            )
   )
)
```

Using rate control parameters, you can also enable retries for other errors additionally to `SlackApiRateLimitedError`:

```
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.ratectrl._

// Creating a client instance with throttling
val client = new SlackApiClient(
   throttler = SlackApiRateThrottler.createStandardThrottler(
        params = 
            SlackApiRateControlParams.StandardLimits.DEFAULT_PARAMS.copy(
                maxRetries = 3 // to retry maximum 3 times,
                retryFor = Set( classOf[SlackApiRateLimitedError], classOf[SlackApiConnectionError] )
            )
   )
)
```
