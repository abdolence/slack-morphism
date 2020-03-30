---
layout: edocs
title: Make Web API calls
permalink: docs/web-api/make-web-api-calls
---
# Slack API client
## Choose an HTTP client backend

You have to choose a [sttp backend](https://sttp.readthedocs.io/en/latest/backends/summary.html) 
that supports `scala.concurrent.Future`, `cats.effect.Async/Effect`, `monix.eval.Task` response wrappers:
* AkkaHttpBackend
* OkHttpFutureBackend
* OkHttpMonixBackend
* HttpClientFutureBackend
* HttpClientMonixBackend
* AsyncHttpClientCatsBackend
* AsyncHttpClientFs2Backend
* Http4sBackend
* AsyncHttpClientMonixBackend

Add a dependency of your choice to your `build.sbt`.

For Akka Http this is:
```
"com.softwaremill.sttp.client" %% "akka-http-backend" % sttpVersion
```
where `sttpVersion` > 2.0+

## Create a client to Slack Web API methods

[SlackApiClient](/api/org/latestbit/slack/morphism/client/SlackApiClient.html) provides access 
to all available of Slack Web API methods.

### Future backend

```scala
// Import Slack Morphism Client
import org.latestbit.slack.morphism.client._

// Import STTP backend
// We're using Akka Http for this example 
import sttp.client.akkahttp.AkkaHttpBackend

// Import support for scala.concurrent.Future implicits from cats (required if you use Future-based backend)
// If you bump into compilation errors that `Future` isn't a `cats.Monad` or `cats.MonadError`, 
// you probably forgot to import this.
// Also, you can import all implicits from cats using `import cats.implicits._` instead of this 
import cats.instances.future._

// Creating an STTP backend
implicit val sttpBackend = AkkaHttpBackend() // this is a Future-based backend

// Creating a client instance
val client = SlackApiClient.create() // or similarly SlackApiClient.create[Future]()
```

### Cats Effect backend

```scala
// Import Slack Morphism Client
import org.latestbit.slack.morphism.client._

// Import STTP backend
// We're using AsyncHttpClientCatsBackend for this example 
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

// Import support for cats-effect
import cats.effect._

// We should provide a ContextShift for Cats IO and STTP backend
implicit val cs: ContextShift[IO] = IO.contextShift( scala.concurrent.ExecutionContext.global )

for {
  backend <- AsyncHttpClientCatsBackend[IO]() // Creating an STTP backend
  client = SlackApiClient.build[IO]( backend ).create() // Create a Slack API client
  result <- client.api.test( SlackApiTestRequest() ) // call an example method inside IO monad
} yield result

```

### Monix backend

```scala
// Import Slack Morphism Client
import org.latestbit.slack.morphism.client._

// Import STTP backend
import sttp.client.asynchttpclient.monix.AsyncHttpClientMonixBackend


// Monix imports 
import monix.eval._
import monix.execution.Scheduler.Implicits.global

for {
        backend <- AsyncHttpClientMonixBackend() // Creating an STTP backend
        client = SlackApiClient.build[Task]( backend ).create() // Create a Slack API client
        result <- client.api.test( SlackApiTestRequest() ) // call an example method inside Task monad
} yield result

```

## Make Web API methods calls

For most of Slack Web API methods (except for OAuth methods, Incoming Webhooks and event replies) 
you need a Slack token to make a call.
For simple bots you can have it in your config files, or you can obtain workspace tokens 
using [Slack OAuth](https://api.slack.com/docs/oauth).

There is an example implementation of Slack OAuth v2 in [Akka Http Example](akka-http).

Slack Morphism requires an implicit token specified as an instance of 
[SlackApiToken](/api/org/latestbit/slack/morphism/client/SlackApiToken.html):

In the example below, we're using a hardcoded Slack token, 
but *don't do that for your production bots and apps*. 
You should securely and properly store all of Slack tokens.
Look at [Slack recommendations](https://api.slack.com/docs/oauth-safety).

```scala
import org.latestbit.slack.morphism.client._
import org.latestbit.slack.morphism.client.reqresp.chat.SlackApiChatPostMessageRequest

import sttp.client.akkahttp.AkkaHttpBackend

implicit val sttpBackend = AkkaHttpBackend()

val client = new SlackApiClient()

implicit val slackApiToken: SlackApiToken = SlackApiBotToken("xoxb-89.....")

client.chat.postMessage(
    SlackApiChatPostMessageRequest(
      channel = "#general",
      text = "Hello Slack"
    )
  ) 
  
```
As you might noticed here, Slack Morphism API mimics Slack Web API method names, so that
[https://slack.com/api/chat.postMessage](https://api.slack.com/methods/chat.postMessage) 
is `client.chat.postMessage(...)`, or [https://api.slack.com/methods/oauth.v2.access](https://api.slack.com/methods/oauth.v2.access) 
is `client.oauth.v2.access(...)` etc.

The complete list of all of the implemented Web API methods is available [here](/api/org/latestbit/slack/morphism/client/SlackApiClient.html).

## Low-level HTTP API to Slack Web API
In case you didn't find a method you need on the list above, 
or you need something different/undocumented/legacy, 
there is a [low-level](/api/org/latestbit/slack/morphism/client/impl/SlackApiHttpProtocolSupport$http$.html) 
API for this scenario:

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
client.http.post[YourRequest,YourResponse](
    methodUri = "some.someMethod", // Slack relative Method URI 
    YourRequest()
)
```

---

Please, don't hesitate to submit a PR with model updates if you find anything missing or find model inconsistency.
This project is open to help each other, so any PRs are welcomed.

---

## Avoid boilerplate making consequent non-blocking client requests with EitherT

This is completely optional and just a recommendation for you.

You might notice some boilerplate when you deal with consequent client requests that returns `Future[ Either[ SlackApiClientError,SomeKindOfSlackResponse ] ]`.

To solve this in a better way, consider using an approach with [EitherT[]](https://typelevel.org/cats/datatypes/eithert.html) from 
[Cats](https://typelevel.org/cats/), well described [here](http://eed3si9n.com/herding-cats/stacking-future-and-either.html).

For example, this example shows two consequence Web API calls:
We make a first async call, find some result in the first response, and 
getting the response result of a next async call. 

```scala
EitherT( client.channels.list( SlackApiChannelsListRequest() ) ).flatMap { channelsResp =>
  channelsResp.channels
    .find( _.flags.is_general.contains( true ) )
    .map { generalChannel =>
      EitherT(
        client.chat.postMessage(
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
