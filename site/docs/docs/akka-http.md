---
layout: edocs
title: Using Slack Morphism with Akka HTTP
permalink: docs/akka-http
---
# Using Slack Morphism with Akka HTTP

To show you a complete solution and give you a quick start, Slack Morphism provides 
you a zero-code required and complete example of a Slack bot, 
written with Slack Morphism and Akka Http.

## Install and configure a Slack bot from scratch

### Prerequisites

* sbt v1.3+ : build and run bot
* [ngrok](https://ngrok.com) : to provide HTTPS tunnels to your dev machine for Events API)

### Install and run ngrok on http port
Run `ngrok` on a port 8080:
```
ngrok http 8080
```
We will need the HTTPS URL from it further, so don't close it and open a second terminal.

### Create a Slack app if you don't have any
Just follow at [Slack dev website](https://api.slack.com/apps) and create an app profile.

We will use OAuth v2/granular permissions/scopes, so:
* Follow to `Tools → Update to Granular Scopes`
* Select the following bot token scopes:
    * `commands`
    * `im:history`, `im:read`, `im:write`
    * `users:read`
    * `team:read`
    * `channels:history`
    * `chat:write:bot` (it might be also `chat:write` after upgrade to Granular Scopes, careful with this one)
    * `users.profile:read`
  
![sbt-example-command](https://slack.abdolence.dev/img/create-bot-token-scopes.png)

So, the who list would be 
`channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read`
and we will use this line further.

### Create your own bot with sbt

* Choose some directory and create your own Slack bot with `sbt` tool: 
```bash
sbt new abdolence/slack-morphism-akka-http.g8
```
![sbt-example-command](https://slack.abdolence.dev/img/create-sbt-bot-command.png)

* Compile and run it with `sbt`:
```bash
sbt "run \
    --slack-app-id <your-app-id> \
    --slack-client-id <your-client-id> \
    --slack-client-secret <your-client-secret> \
    --slack-signing-secret <your-signing-secret> \
    --slack-install-bot-scope channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read \
    --slack-redirect-url https://<your-ngrok-id>.ngrok.io/auth/callback"
```
Grab all required credentials from `Slack App Profile → Settings → Basic Information`.
If you all this correctly you should see something like this:

![sbt-example-command](https://slack.abdolence.dev/img/bot-run-example.png)

### Fill in profile parameters in your Slack app profile

* `Features → Event Subscriptions → Enable Events`
    * Request URL: 
        ```
        https://<your-ngrok-id>.ngrok.io/push
        ```
    * Subscribe to bot events: `app_home_opened`, `message.im`, `message.channels`
    * **Save Changes**
    
![sbt-example-command](https://slack.abdolence.dev/img/event-subscriptions.png)

* `Features → OAuth & Permissions`:
    * Redirect URLs: 
        ```
        https://<your-ngrok-id>.ngrok.io/auth/callback
        ```
    * **Save URLs**
    * Check if `Scopes → Bot Token Scopes` contains all required scopes:
        `channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read`
      (occasionally Slack lose something in this list after upgrading to Granular Scopes)  

* `Features → Interactive Components → Interactivity`:
    * Request URL:
    ```
    https://<your-ngrok-id>.ngrok.io/interaction
    ```     

* `Features → Home`:
    * Click on **Sign Up**
    
### Install a bot into your workspace using Slack OAuth v2
Just follow to the 
```
http://localhost:8080/auth/install
```
in your browser and allow the installation.

That's it, you now have a working bot in your workspace written with Scala, Slack Morphism and Akka.
Follow to the `Apps` section in your workspace. 
You should see your app with its `Home tab` and try to send a message to it, 
or click on buttons on messages and views.

## The code structure overview
Now it is probably time to open a bot project in IDE to have deeper look how it works inside:

* Akka HTTP routes are located in `routes` subpackage, where you can find an auxiliary trait
`AkkaHttpServerRoutesSupport` that helps you to verify Slack Events signature 
and read tokens for workspace.
* `SlackInteractionEventsRoute` implements a handler for Slack Blocks Interactions Events (like clicking on buttons).
* `SlackPushEventsRoute` implements a handler for Slack Push Events (like incoming messages or opening tabs).
* `SlackOAuthRoutes` implements OAuth v2 including redirection to Slack and back.

For simplicity sake:
   * This template uses an embedded database [SwayDb](http://swaydb.io/) to store tokens. 
   For production environments, you should consider to use more appropriate solutions for your case.   
   * This template doesn't provide HTTPS configuration for Akka, which is described in detail [here](https://doc.akka.io/docs/akka-http/current/server-side/server-https-support.html) 
   or you should consider other HTTPS solutions (like reverse HTTP proxies).
   