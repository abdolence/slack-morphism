---
layout: edocs
title: Slack Events API
permalink: docs/events-api
---
# Slack Events API

To help you implement [Slack Events API](https://api.slack.com/events-api) listener, Slack Morphism provides you:

* Well-typed events model and ready to use JSON encoder/decoders
* Slack events signature verifier, to verify requests from Slack with confidence using your signing secret
* Additional Web API client methods to post replies to events back

Because Slack Morphism is a Web-framework agnostic, you are free to choose any you're familiar with.

However, to simplify understanding and show how to use it, Slack Morphism provides 
[a full-featured bot for Akka HTTP](akka-http), which implements Events API with OAuth.

## Slack Events API Model

There are following types of Events

- [SlackPushEvent](/api/org/latestbit/slack/morphism/events/SlackPushEvent.html) for all push events (like incoming messages, opening tabs, installing/uninstalling app, etc)
- [SlackInteractionEvent](/api/org/latestbit/slack/morphism/events/SlackInteractionEvent.html) for all interaction events from Slack (actions on messages and views)

JSON encoders and decoders for [Circe](http://circe.io/) available either using
```
import org.latestbit.slack.morphism.codecs.implicits._

```
or using a trait:
```
trait MyHttpRoutesSupport extends org.latestbit.slack.morphism.codecs.CirceCodecs {
  ...
}
```

and you can decode all of the Slack Events like:

```scala
    decode[SlackInteractionEvent]( requestBody ) match {
      case Right( event ) => // ...
      case Left( ex ) => {
        logger.error( s"Can't decode push event from Slack: ${ex.toString}\n${requestBody}" )
        complete( StatusCodes.BadRequest )
      }
    }
```


## Slack events signature verifier

To verify requests from Slack using your signing secret, there is [SlackEventSignatureVerifier](/api/org/latestbit/slack/morphism/events/signature/SlackEventSignatureVerifier.html), 
which provides you `verify()`:

To verify event request you need:
* A complete HTTP request body
* The following HTTP header values from request:
    * SlackEventSignatureVerifier.HttpHeaderNames.SIGNED_TIMESTAMP
    * SlackEventSignatureVerifier.HttpHeaderNames.SIGNED_HASH
* Your signing secret from your Slack app profile

```scala
val signatureVerifier = new SlackEventSignatureVerifier()

signatureVerifier.
    verify(config.signingSecret, receivedSignedHash, receivedSignedTimestamp, requestBody) match {
        case Right(success) => // the signature is verified
        case Left(err) => // absent or wrong signature    
    }
```