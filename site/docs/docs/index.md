---
layout: edocs
title: Intro
permalink: docs/
---
# Intro and motivations

### Type-safety
Scala is a static-typed language, and Slack Morphism provides methods and models definitions for most of 
the methods and structures of Slack Web/Events API.
There is a [well-typed DSL](templating) to build your messages and views with Slack blocks safely as well. 
 
### Easy to use
While Slack Morphism uses some of FP paradigms and mostly avoids imperative style and mutable states internally, 
its API doesn't expose your app anything that might make you learn additional advanced frameworks (like ZIO, cats-effects, and others).
It provides you with a simple API with the support for Scala Future, Option, Either, etc.
If you're into pure functional concepts, there is support for cats-effect IO as well.

### Frameworks-agnostic
Slack Morphism core library intentionally avoid dependencies to any additional Web-frameworks, so 
you free to use any framework you're familiar with.
Slack Morphism doesn't have any dependencies to any particular HTTP client library either 
and uses [sttp library](https://github.com/softwaremill/sttp) to give you choice.

However, it would be an incomplete solution to provide you only core components, and for a quick start, 
there are full-featured giter8 templates and examples for:
* [Slack bot with Akka HTTP](https://github.com/abdolence/slack-morphism-akka-http.g8), 
which is implemented in a conservative way with classic Scala Futures.
* [Slack bot with http4s](https://github.com/abdolence/slack-morphism-http4s.g8), 
which is implemented in a pure functional way with cats-effect effects and FS2 streams.

You can browse source codes of examples (without installing them) [here](https://github.com/abdolence/slack-morphism/tree/master/examples).
 
### Non-blocking and Reactive
All of the Slack Morphism API methods available in a non-blocking manner, and 
some of them (e.g. returning Slack channel history) also have support for a reactive Publisher,
 if you're familiar with Reactive Streams (and using related frameworks).
 
### Rate controlling and throttling access to Slack Web API
There is ready to use a rate control/throttling implementation, 
which follows the Slack API rate limits and tiers (or your custom configuration).

## Getting Started
Add the following to your `build.sbt`:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-client" % "1.2.3"
```

or if you'd like to full-featured and ready to use Slack bots, use this:

### Akka HTTP
```
sbt new abdolence/slack-morphism-akka-http.g8
```
Read more about this template [here](akka-http).

### http4s
```
sbt new abdolence/slack-morphism-http4s.g8
```
Read more about this template [here](http4s).

## Limitations

Slack Morphism doesn't provide:
- RTM API (the usage of which is slowly declining in favour of Events API)
- Legacy Web/Events API methods and models (like Slack Message attachments, which should be replaced with Slack Blocks)
