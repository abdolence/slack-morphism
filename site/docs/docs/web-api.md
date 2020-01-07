---
layout: edocs
title: Slack Web API
permalink: docs/web-api
---
## Slack Web API

### Creating client to Slack Web API methods

[SlackApiClient](/api/org/latestbit/slack/morphism/client/SlackApiClient.html) provides access 
to all available of Slack Web API methods.

```scala
// Import Slack Morphism Client
import org.latestbit.slack.morphism.client._

// Import STTP backend that supports async HTTP access via Future
// We're using Akka Http for this example 
import sttp.client.akkahttp.AkkaHttpBackend

// Creating STTP backend
implicit val sttpBackend = AkkaHttpBackend()

// Creating client instance
val slackApiClient = new SlackApiClient()
```

### Making Web API calls

To make calls to Slack Web API methods (except OAuth methods) you need a Slack token.
For simple bots you can have it in your config files, or you can obtain tokens for workspaces 
using [Slack OAuth](https://api.slack.com/docs/oauth).

There is an example implementation of Slack OAuth v2 in [Akka Http Example](akka-http).

Slack Morphism requires implicit token specified as an instance of 
[SlackApiToken](/api/org/latestbit/slack/morphism/client/SlackApiToken.html):

In an example below, we're using a hardcoded Slack token, but *don't do that for your production bots and apps*.
You should securely and properly store all of Slack tokens.
Look at [Slack recommendations](https://api.slack.com/docs/oauth-safety).

```scala
import org.latestbit.slack.morphism.client._

import sttp.client.akkahttp.AkkaHttpBackend
implicit val sttpBackend = AkkaHttpBackend()

implicit val slackApiToken : SlackApiToken = SlackApiToken.createFrom(
  SlackApiToken.TokenTypes.BOT,
  "xoxb-89....."
)

val slackApiClient = new SlackApiClient()

SlackApiToken
      .createFrom(
        SlackApiToken.TokenTypes.BOT,
        "xoxb-89....."
      )
      .foreach { implicit slackApiToken: SlackApiToken =>
            slackApiClient.chat
              .postMessage(
                SlackApiChatPostMessageRequest(
                  channel = "#general",
                  text = "Hello Slack"
                )
              )
      }
  
```
As you might noticed here, Slack Morphism API mimics Slack Web API method names, so that
[https://slack.com/api/chat.postMessage](https://api.slack.com/methods/chat.postMessage) 
is `slackApiClient.chat.postMessage()`, or [https://api.slack.com/methods/oauth.v2.access](https://api.slack.com/methods/oauth.v2.access) 
is `slackApiClient.oauth.v2.access()` etc.

The complete list of all of the implemented Web API methods is available [here](/api/org/latestbit/slack/morphism/client/SlackApiClient.html).

### Low-level HTTP API to Slack Web API
In case you didn't find a method you need on the list above, there is [low-level](/api/org/latestbit/slack/morphism/client/impl/SlackApiHttpProtocolSupport$http$.html) API for this case:

```scala
implicit slackApiToken: SlackApiToken = ...

// Definition of your request and response as a case classes
case class YourRequest(...)
case class YourResponse(...)

// Definitions of JSON encoder/decoders
import io.circe.generic.semiauto._

implicit val yourRequestEncoder = deriveEncoder[YourRequest] 
implicit val yourResponseDecoder = deriveEncoder[YourRequest]

// Make a call
slackApiClient.http.post[YourRequest,YourResponse](
    methodUri = "some.someMethod", // Slack relative Method URI 
    YourRequest()
)
``` 

### Working with pagination/batching
Some of the Web API methods defines cursors and pages to give you an ability to load a lot of data
continually (using batching approach, making many requests).

Examples:
* [conversations.history](https://api.slack.com/methods/conversations.history)
* [conversations.list](https://api.slack.com/methods/conversations.list)
* [users.list](https://api.slack.com/methods/users.list)
* ...

To help with those methods Slack Morphism provides a "scroller" implementation, which deal with 
all scrolling/batching requests for you.

With this scroller you have the following choice:

* Load data lazily, but synchronously data to a standard Scala lazy container: Stream[] (Scala 2.12) / LazyList[] (Scala 2.13+)
* Load data lazily and asynchronously with Async Iterator (implemented by Slack Morphism)
* Load data reactively with Publisher[] and use Reactive Streams  

For example, for [conversations.history](https://api.slack.com/methods/conversations.history) you can:

#### Load data into Stream[]/LazyList[]
```scala

// Synchronous approach (all batches would be loaded with blocking)
slackApiClient.conversations
  .historyScroller(
    SlackApiConversationsHistoryRequest(
      channel = "C222...." // some channel id
    )
  )
  .toSyncScroller( 5.seconds )
  .foreach {
    case Right( results: LazyList[SlackMessage] ) => {
      // ... results are ready to scroll
    }
    case Left( err ) => // the first batch is failed here
  }

```

#### Folding Using Lazy/Async iterator
```scala

slackApiClient.conversations
  .historyScroller(
    SlackApiConversationsHistoryRequest(
      channel = "C222...." // some channel id
    )
  )
  .toAsyncScroller()
  .foldLeft( List[SlackMessage]() ) {
    case ( wholeList, futureRes ) =>
      futureRes.map( wholeList ++ _ ).getOrElse( wholeList ) 
    // futureRes is Either[SlackApiClientError, List[SlackMessage]]
  }
```

#### Create a reactive Publisher[]
```scala

val Publisher[SlackMessage] = 
slackApiClient.conversations
  .historyScroller(
    SlackApiConversationsHistoryRequest(
      channel = "C222...." // some channel id
    )
  )
  .toPublisher()

// use it with your reactive frameworks (like Akka Streams, etc)

```

#### Avoid boilerplate making consequent non-blocking client requests with EitherT

This is completely optional and just a recommendation for you.

You might notice some boilerplate when you deal with consequent client requests 
that returns `Future[Either[SlackApiClientError,SomeKindOfSlackResponse]]`.

To deal with that, consider using an approach with [EitherT[]](https://typelevel.org/cats/datatypes/eithert.html) from 
[Cats](https://typelevel.org/cats/), well described [here](http://eed3si9n.com/herding-cats/stacking-future-and-either.html).

For example, this example shows two consequence Web API calls:
We make a first async call, find some result in the first response, and 
getting the response result of a next async call. 

```scala
EitherT( slackApiClient.channels.list( SlackApiChannelsListRequest() ) ).flatMap { channelsResp =>
  channelsResp.channels
    .find( _.flags.is_general.contains( true ) )
    .map { generalChannel =>
      EitherT(
        slackApiClient.chat
          .postMessage(
            SlackApiChatPostMessageRequest(
              channel = generalChannel.id,
              text = "Hello"
            )
          )
      ).map { resp =>
        resp.ts.some
      }
    }
    .getOrElse(
      EitherT[Future, SlackApiClientError, Option[String]](
        Future.successful( None.asRight )
      )
    )
}
```

EitherT gives you the power to deal with those transformations and avoid boilerplate 
without sacrificing error handling.
