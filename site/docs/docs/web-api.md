---
layout: edocs
title: Slack Web API
permalink: docs/web-api
---
## Slack Web API

### Choose an HTTP client backend

You should choose a [sttp backend](https://sttp.readthedocs.io/en/latest/backends/summary.html) 
that supports `scala.concurrent.Future` responses:
* AkkaHttpBackend
* OkHttpFutureBackend
* HttpClientFutureBackend

Add a dependency of your choice to your `build.sbt`.

For Akka Http this is:
```
"com.softwaremill.sttp.client" %% "akka-http-backend" % sttpVersion
```

### Create a client to Slack Web API methods

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
For simple bots you can have it in your config files, or you can obtain workspace tokens 
using [Slack OAuth](https://api.slack.com/docs/oauth).

There is an example implementation of Slack OAuth v2 in [Akka Http Example](akka-http).

Slack Morphism requires an implicit token specified as an instance of 
[SlackApiToken](/api/org/latestbit/slack/morphism/client/SlackApiToken.html):

In the example below, we're using a hardcoded Slack token, but *don't do that for your production bots and apps*.
You should securely and properly store all of Slack tokens.
Look at [Slack recommendations](https://api.slack.com/docs/oauth-safety).

```scala
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.reqresp.chat.SlackApiChatPostMessageRequest

import sttp.client.akkahttp.AkkaHttpBackend

implicit val sttpBackend = AkkaHttpBackend()
val slackApiClient = new SlackApiClient()

implicit val slackApiToken: SlackApiToken = SlackApiBotToken("xoxb-89.....")

slackApiClient.chat
  .postMessage(
    SlackApiChatPostMessageRequest(
      channel = "#general",
      text = "Hello Slack"
    )
  ) 
  
```
As you might noticed here, Slack Morphism API mimics Slack Web API method names, so that
[https://slack.com/api/chat.postMessage](https://api.slack.com/methods/chat.postMessage) 
is `slackApiClient.chat.postMessage()`, or [https://api.slack.com/methods/oauth.v2.access](https://api.slack.com/methods/oauth.v2.access) 
is `slackApiClient.oauth.v2.access()` etc.

The complete list of all of the implemented Web API methods is available [here](/api/org/latestbit/slack/morphism/client/SlackApiClient.html).

### Low-level HTTP API to Slack Web API
In case you didn't find a method you need on the list above, there is [low-level](/api/org/latestbit/slack/morphism/client/impl/SlackApiHttpProtocolSupport$http$.html) API for this case:

```scala
import org.latestbit.slack.morphism.client._

// Definition of your request and response as a case classes
case class YourRequest(...)
case class YourResponse(...)

// Definitions of JSON encoder/decoders
import io.circe.generic.semiauto._

implicit val yourRequestEncoder = deriveEncoder[YourRequest] 
implicit val yourResponseDecoder = deriveEncoder[YourRequest]

// Need init a token before making calls
implicit slackApiToken: SlackApiToken = ...

// Make a call
slackApiClient.http.post[YourRequest,YourResponse](
    methodUri = "some.someMethod", // Slack relative Method URI 
    YourRequest()
)
```
---
Please, don't hesitate to submit a PR with model updates if you find anything missing or find model inconsistency.
This project is open to help each others, so any PRs are welcomed.
---

### Working with pagination/batching results
Some of the Web API methods defines cursors and [pagination](https://api.slack.com/docs/pagination), to give you an ability to load a lot of data
continually (using batching and making many requests).

Examples:
* [conversations.history](https://api.slack.com/methods/conversations.history)
* [conversations.list](https://api.slack.com/methods/conversations.list)
* [users.list](https://api.slack.com/methods/users.list)
* ...

To help with those methods Slack Morphism provides a "scroller" implementation, which deal with 
all scrolling/batching requests for you.

With this scroller you have the following choice:

* Load data lazily, but synchronously data to a standard Scala lazy container: Stream[] (for Scala 2.12) or [LazyList[]](https://www.scala-lang.org/api/current/scala/collection/immutable/LazyList.html) (for Scala 2.13+)
* Load data lazily and asynchronously with [AsyncSeqIterator](/api/org/latestbit/slack/morphism/concurrent/AsyncSeqIterator.html)
* Load data reactively with [Publisher[]](https://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html) and use Reactive Streams   

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

#### Using lazy AsyncSeqIterator to scroll data

Async iterator implements:
* `foldLeft` for accumulating batching results if you need it (the implementation of AsyncSeqIterator doesn't memoize like Stream/LazyList)
* `map` to transform results
* `foreach` to iterate with effects

This is an example of using `foldLeft`:
```scala

slackApiClient.conversations
  .historyScroller(
    SlackApiConversationsHistoryRequest(
      channel = "C222...." // some channel id
    )
  )
  .toAsyncScroller()
  .foldLeft( List[SlackMessage]() ) {
    // futureRes here is a batch result defined as Either[SlackApiClientError, List[SlackMessage]]
    case ( wholeList, futureRes ) =>
      futureRes.map( wholeList ++ _ ).getOrElse( wholeList )     
  }
```

#### Create a reactive Publisher[]
```scala
import org.reactivestreams.Publisher


val publisher : Publisher[SlackMessage] = 
    slackApiClient.conversations
      .historyScroller(
        SlackApiConversationsHistoryRequest(
          channel = "C222...." // some channel id
        )
      )
      .toPublisher()

// Now you can use it as any other publishers with your reactive frameworks (like Akka Streams, etc)

// This is an example of Akka Streams
// https://doc.akka.io/docs/akka/2.5.3/scala/stream/stream-integrations.html#integrating-with-reactive-streams
import akka.stream._
import akka.stream.scaladsl._

Source
    .fromPublisher(publisher)
    .runForeach(msg => println(msg))

```

#### Avoid boilerplate making consequent non-blocking client requests with EitherT

This is completely optional and just a recommendation for you.

You might notice some boilerplate when you deal with consequent client requests 
that returns `Future[Either[SlackApiClientError,SomeKindOfSlackResponse]]`.

To solve this in a better way, consider using an approach with [EitherT[]](https://typelevel.org/cats/datatypes/eithert.html) from 
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
