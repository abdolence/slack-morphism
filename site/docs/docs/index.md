---
layout: edocs
title: Intro
permalink: docs/
---
# Intro and motivations

### Type-safety
Scala is a static-typed language, and Slack Morphism provides quite a lot of definitions of 
most of the available methods and structures of Slack Web/Events API, 
just to help you find errors at compile time.
There is a [well-typed DSL](blocks-templating.md) for it to build your messages and views with Slack blocks safely. 
 
### Easy to use
While Slack Morphism uses some of FP paradigms and avoid imperative style and mutable states internally, 
its API doesn't expose to you anything that might make you learn advanced additional frameworks 
(like ZIO, cats-effects, etc).
It provides you an API mostly based on Scala Future, Option, Either, etc.

### Frameworks-agnostic
Slack Morphism core library intentionally avoid dependencies to any additional Web Frameworks, so 
you free to use any framework you're familiar with.
Slack Morphism doesn't have any dependencies to any particular HTTP client library either 
and uses [sttp library](https://github.com/softwaremill/sttp) to give you choice of options.

However, it would be an incomplete solution to provide you only core components, and for a quick start, 
there is a [full-featured giter8 template and example](https://github.com/abdolence/slack-morphism-akka-http.g8) 
of a Slack Bot built with Slack Morphism and Akka Http. 

## Getting Started
Add the following to your `build.sbt`:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-client" % "1.0.0"
```

or if you'd like to full-featured and ready to use Slack bot, which uses Akka Http, use this:

```
sbt new abdolence/slack-morphism-akka-http
```
