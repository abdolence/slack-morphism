---
layout: edocs
title: Getting Started
permalink: docs/getting-started
---

# Getting Started
Add the following to your `build.sbt`:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-client" % "1.2.3"
```

or if you'd like to start with a full-featured example and template of a Slack bot. that supports:
* Slack Events API, including interactivity controls, commands, home tab and shortcuts (actions)
* Slack Blocks in messages and modals
* Slack OAuth2 interaction and Slack API tokens management

![sbt-example-command](https://slack.abdolence.dev/img/sample-bot-features.png)

## Start with Akka HTTP
The template is using Slack-Morphism with Akka HTTP and classic scala Futures.

```
sbt new abdolence/slack-morphism-akka-http.g8
```
Read more about this template [here](akka-http).

## Start with http4s
The template is using Slack-Morphism in a pure functional way with http4s and cats-effect.

```
sbt new abdolence/slack-morphism-http4s.g8
```
Read more about this template [here](http4s).