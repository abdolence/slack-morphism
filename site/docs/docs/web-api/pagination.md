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

To help with those methods Slack Morphism provides a "scroller" implementation, which deal with 
all scrolling/batching requests for you.

With this scroller you have the following choice:

* Load data lazily, but synchronously into a standard Scala lazy container: Stream[] (for Scala 2.12) or [LazyList[]](https://www.scala-lang.org/api/current/scala/collection/immutable/LazyList.html) (for Scala 2.13+)
* Load data lazily and asynchronously with [AsyncSeqIterator](/api/org/latestbit/slack/morphism/concurrent/AsyncSeqIterator.html)
* Load data reactively with [Publisher[]](https://www.reactive-streams.org/reactive-streams-1.0.3-javadoc/org/reactivestreams/Publisher.html) and use Reactive Streams   

For example, for [conversations.history](https://api.slack.com/methods/conversations.history) you can:

## Load data into Stream[]/LazyList[]
```scala

// Synchronous approach (all batches would be loaded with blocking)
client.conversations.historyScroller(
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

## Using lazy AsyncSeqIterator to scroll data

Async iterator implements:
* `foldLeft` for accumulating batching results if you need it (the implementation of AsyncSeqIterator doesn't memoize like Stream/LazyList)
* `map` to transform results
* `foreach` to iterate with effects

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

## Create a reactive Publisher[]
```scala
import org.reactivestreams.Publisher


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