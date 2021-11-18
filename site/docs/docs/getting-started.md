---
layout: edocs
title: Getting Started
permalink: docs/getting-started
---

# Getting Started
Add the following to your `build.sbt`:

```scala
libraryDependencies += "org.latestbit" %% "slack-morphism-client" % "4.0.1"
```

Have a look at full-featured examples for Akka HTTP and http4s [here](https://github.com/abdolence/slack-morphism/tree/master/examples), which supports:
* Slack Events API, including interactivity controls, commands, home tab and shortcuts (actions)
* Slack Blocks in messages and modals
* Slack OAuth2 interaction and Slack API tokens management

![sbt-example-command](https://slack.abdolence.dev/img/sample-bot-features.png)

