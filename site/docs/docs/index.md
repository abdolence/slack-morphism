---
layout: edocs
title: Intro
permalink: docs/
---
# Intro and motivations

### Type-safety
Scala is a static-typed language, and Slack Morphism provides methods and models definitions for most of 
the methods and structures of Slack Web/Events API.
There is a [well-typed DSL](Blocks DSL) to build your messages and views with Slack blocks safely as well. 
 
### Easy to use
While Slack Morphism uses some of FP paradigms and avoids imperative style and mutable states internally, 
its API doesn't expose to you anything that might make you learn additional advanced frameworks 
(like ZIO, cats-effects, and others).
It provides you with a simple API mostly based on Scala Future, Option, Either, etc.

### Frameworks-agnostic
Slack Morphism core library intentionally avoid dependencies to any additional Web-frameworks, so 
you free to use any framework you're familiar with.
Slack Morphism doesn't have any dependencies to any particular HTTP client library either 
and uses [sttp library](https://github.com/softwaremill/sttp) to give you choice of options.

However, it would be an incomplete solution to provide you only core components, and for a quick start, 
there is a [full-featured giter8 template and example](https://github.com/abdolence/slack-morphism-akka-http.g8) 
of a Slack Bot built with Slack Morphism and Akka Http. 

### Non-blocking and Reactive
All of the Slack Morphism API methods available in a non-blocking manner, and 
some of them (e.g. returning Slack channel history) also have support for a reactive Publisher,
 if you're familiar with Reactive Streams (and using related frameworks).

## Getting Started
Add the following to your `build.sbt`:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-client" % "1.0.2"
```

or if you'd like to full-featured and ready to use Slack bot, which uses Akka Http, use this:

```
sbt new abdolence/slack-morphism-akka-http.g8
```
Read more about this template [here](akka-http).

## Limitations

Slack Morphism doesn't provide:
- RTM API (the usage of which is slowly declining in favour of Events API)
- Legacy Web/Events API methods and models (like Slack Message attachments, which should be replaced with Slack Blocks)
