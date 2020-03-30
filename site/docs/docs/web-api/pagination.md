---
layout: edocs
title: Pagination and scrolling results
permalink: docs/web-api/pagination
---
# Working with pagination/batching results
Some Web API methods defines cursors and [pagination](https://api.slack.com/docs/pagination), to give you an ability to load a lot of data
continually (using batching and continually making many requests).

Examples:
* [conversations.history](https://api.slack.com/methods/conversations.history)
* [conversations.list](https://api.slack.com/methods/conversations.list)
* [users.list](https://api.slack.com/methods/users.list)
* ...

To help with those methods Slack Morphism provides additional "scroller" implementation, which deal with 
all scrolling/batching requests for you.

With this scroller you have the following choice:

* Load data lazily and asynchronously with [AsyncSeqIterator](/api/org/latestbit/slack/morphism/concurrent/AsyncSeqIterator.html)
* Load data with [FS2 streams](https://fs2.io/)
* Load data with [Reactive Streams Publisher[]](https://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html)
* Load data lazily, but synchronously into a standard Scala lazy container: Stream[] (for Scala 2.12) or [LazyList[]](https://www.scala-lang.org/api/current/scala/collection/immutable/LazyList.html) (for Scala 2.13+)

If none of those approaches are suitable for you, you can always use original API method. 
Scrollers are completely optional.

For example, for [conversations.history](https://api.slack.com/methods/conversations.history) you can:

## Using a built-in lazy AsyncSeqIterator to scroll data

Async iterator implements:
* `foldLeft` for accumulating batching results if you need it (the implementation of AsyncSeqIterator doesn't memoize like Stream/LazyList)
* `map` to transform results
* `foreach` to iterate with effects

(It is implemented as a very simple and pure functor and provides `cats.Functor` instance as well).

This is an example of using `foldLeft`:
```scala

client.conversations.historyScroller(
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

## Create a FS2 stream
This is an optional module, and if you're using FS2, you need additional dependency:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-fs2" % slackMorphismVer
```

Then you can use it as following:

```scala

// Additional import for support FS2
import org.latestbit.slack.morphism.client.fs2s._

client.conversations.historyScroller(
    SlackApiConversationsHistoryRequest(
      channel = "C222...." // some channel id
    )
  )
  .toFs2Scroller().compile.toList.unsafeRunSync() 

``` 

## Create a reactive stream Publisher[]
This is an optional module, and if you're using Reactive Streams, you need additional dependency:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-reactive-streams" % slackMorphismVer
```

Then you can use it as in this example:

```scala
import org.reactivestreams.Publisher

import cats.instances.future._ // or cats.implicits._ required
import scala.concurrent.ExecutionContext.Implicits.global

// Additional import for support Reactive Streams
import org.latestbit.slack.morphism.client.reactive._


val publisher : Publisher[SlackMessage] = 
    client.conversations.historyScroller(
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

## Load data into Stream[]/LazyList[]

Because of the nature of Scala Collection API, there are limitations:

* this is synchronous approach and blocking operation, so when you scroll through LazyList[] there are blocking operations (at the moment of a batch loading).
* you won't get any errors except for the first batch.

Don't use this for huge load scenarios, and it rather was created for testing purposes.

```scala

// Synchronous approach (all batches would be loaded with blocking)
client.conversations.historyScroller(
    SlackApiConversationsHistoryRequest(
      channel = "C222...." // some channel id
    )
  )
  .toSyncScroller( 30.seconds )
  .foreach {
    case Right( results: LazyList[SlackMessage] ) => {
      // ... results are ready to scroll
    }
    case Left( err ) => // the first batch is failed here
  }

```
