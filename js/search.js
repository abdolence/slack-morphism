// When the user clicks on the search box, we want to toggle the search dropdown
function displayToggleSearch(e) {
  e.preventDefault();
  e.stopPropagation();

  closeDropdownSearch(e);
  
  if (idx === null) {
    console.log("Building search index...");
    prepareIdxAndDocMap();
    console.log("Search index built.");
  }
  const dropdown = document.querySelector("#search-dropdown-content");
  if (dropdown) {
    if (!dropdown.classList.contains("show")) {
      dropdown.classList.add("show");
    }
    document.addEventListener("click", closeDropdownSearch);
    document.addEventListener("keydown", searchOnKeyDown);
    document.addEventListener("keyup", searchOnKeyUp);
  }
}

//We want to prepare the index only after clicking the search bar
var idx = null
const docMap = new Map()

function prepareIdxAndDocMap() {
  const docs = [  
    {
      "title": "Using Slack Morphism with Akka HTTP",
      "url": "/docs/akka-http",
      "content": "Using Slack Morphism with Akka HTTP To show you a complete solution and give you a quick start, Slack Morphism provides you a zero-code required and complete example of a Slack bot, written with Slack Morphism and Akka Http. You can browse source codes of a bot example (without installing it) here. Install and configure a Slack bot from scratch Prerequisites sbt v1.3+ : build and run bot ngrok : to provide HTTPS tunnels to your dev machine for Events API) Install and run ngrok on http port Run ngrok on a port 8080: ngrok http 8080 We will need the HTTPS URL from it further, so don’t close it and open a second terminal. Create a Slack app if you don’t have any Just follow at Slack dev website and create an app profile. We will use OAuth v2/granular permissions/scopes, so: Follow to Tools → Update to Granular Scopes Select the following bot token scopes: commands im:history, im:read, im:write users:read team:read channels:history chat:write:bot (it might be also chat:write after upgrade to Granular Scopes, careful with this one) users.profile:read So, the who list would be channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read and we will use this line further. Create your own bot with sbt Choose some directory and create your own Slack bot with sbt tool: sbt new abdolence/slack-morphism-akka-http.g8 Compile and run it with sbt: sbt \"run \\ --slack-app-id &lt;your-app-id&gt; \\ --slack-client-id &lt;your-client-id&gt; \\ --slack-client-secret &lt;your-client-secret&gt; \\ --slack-signing-secret &lt;your-signing-secret&gt; \\ --slack-install-bot-scope channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read \\ --slack-redirect-url https://&lt;your-ngrok-id&gt;.ngrok.io/auth/callback\" Grab all required credentials from Slack App Profile → Settings → Basic Information. If you all this correctly you should see something like this: Fill in profile parameters in your Slack app profile Features → Event Subscriptions → Enable Events Request URL: https://&lt;your-ngrok-id&gt;.ngrok.io/push Subscribe to bot events: app_home_opened, message.im, message.channels Save Changes Features → OAuth &amp; Permissions: Redirect URLs: https://&lt;your-ngrok-id&gt;.ngrok.io/auth/callback Save URLs Check if Scopes → Bot Token Scopes contains all required scopes: channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read (occasionally Slack lose something in this list after upgrading to Granular Scopes) Features → Interactive Components → Interactivity: Request URL: https://&lt;your-ngrok-id&gt;.ngrok.io/interaction Features → Home: Click on Sign Up Install a bot into your workspace using Slack OAuth v2 Just follow to the http://localhost:8080/auth/install in your browser and allow the installation. That’s it, you now have a working bot in your workspace written with Scala, Slack Morphism and Akka. Follow to the Apps section in your workspace. You should see your app with its Home tab and try to send a message to it, or click on buttons on messages and views. The code structure overview Now it is probably time to open a bot project in IDE to have deeper look how it works inside: Akka HTTP routes are located in routes subpackage, where you can find an auxiliary trait AkkaHttpServerRoutesSupport that helps you to verify Slack Events signature and read tokens for workspace. SlackInteractionEventsRoutes implements a handler for Slack Blocks Interactions Events (like clicking on buttons). SlackPushEventsRoutes implements a handler for Slack Push Events (like incoming messages or opening tabs). SlackOAuthRoutes implements OAuth v2 including redirection to Slack and back. For simplicity sake: This template uses an embedded database SwayDb to store tokens. For production environments, you should consider to use more appropriate solutions for your case. This template doesn’t provide HTTPS configuration for Akka, which is described in detail here or you should consider other HTTPS solutions (like reverse HTTP proxies)."
    } ,    
    {
      "title": "Slack Events API",
      "url": "/docs/templating",
      "content": "Slack Blocks Templating DSL Simple to use and type-safe DSL for Slack Blocks. Intro Slack Blocks are visual components that can be stacked and arranged to create app layouts in messages and views. Read official docs for introduction to Slack Block Kit. Let’s take some very simple block example: { \"blocks\": [ { \"type\": \"section\", \"text\": { \"type\": \"mrkdwn\", \"text\": \"A message *with some bold text* and _some italicized text_.\" } } ] } Now, let’s look at how it might look with type-safe Scala code using Slack Morphism Blocks DSL: blocks( sectionBlock( text = md\"A message *with some bold text* and _some italicized text_.\" ) ) Using Slack Morphism Blocks DSL you can embed those definitions into your Slack messages and views, and DSL also provides you both compile time and runtime validations for structure and formatting rules defined for Slack Blocks, using Scala language features and Design by contract (DbC) principles. Let’s look at another more complex example: Json: { \"blocks\": [ { \"type\": \"section\", \"text\": { \"type\": \"mrkdwn\", \"text\": \"&lt;https://example.com|Overlook Hotel&gt; \\n :star: \\n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s.\" }, \"accessory\": { \"type\": \"image\", \"image_url\": \"https://is5-ssl.mzstatic.com/image/thumb/Purple3/v4/d3/72/5c/d3725c8f-c642-5d69-1904-aa36e4297885/source/256x256bb.jpg\", \"alt_text\": \"Haunted hotel image\" } }, { \"type\": \"section\", \"fields\": [ { \"type\": \"mrkdwn\", \"text\": \"*Average Rating*\\n1.0\" }, { \"type\": \"mrkdwn\", \"text\": \"*Updated*\\n&lt;!date^1578400713^{date_pretty}|07 Jan 2020 12:38:33 GMT&gt;\" } ] }, { \"type\": \"context\", \"elements\": [ { \"type\": \"mrkdwn\", \"text\": \"*Author:* T. M. Schwartz\" } ] } ] } It would be using DSL defined as: blocks( sectionBlock( text = md\"${formatUrl( \"https://example.com\", \"Overlook Hotel\" )}\\n :star: \\n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s.\" ), sectionBlock( fields = sectionFields( md\"*Average Rating*\\n1.0\", md\"*Updated*\\n${formatDate(Instant.now())}\" ) ), contextBlock( blockElements( md\"*Author:* T. M. Schwartz\" ) ) ) Text object formatting There are specialised DSL string interpolators: md\"Mark down Text\" : creates Slack mrkdwn text object pt\"Plain text\" : creates Slack plain text object Optional blocks and elements There are very useful DSL terms to provide an optional block, element, field or a choice item depends on some user defined condition (a predicate): optionally - single optional item (block, block element, section field, choice item, choice group) optBlocks - optional list of blocks The optionally function evaluates lazily its second parameter, so it might be useful as well to know. You can use them in Scala as: // Conditional item of overflow menu: sectionBlock( text = md\"Test 2\", accessory = overflow( action_id = SlackActionId(\"-\"), options = choiceItems( choiceItem( text = pt\"test-menu-item 1\", value = \"1\" ), choiceItem( text = pt\"test-menu-item 2\", value = \"2\" ), optionally( someUserParam &gt; 0 ) ( choiceItem( text = pt\"conditional-menu-item 3\", value = \"3\" ) ) ) ) ) // Conditional group of blocks example optBlocks( someParam &gt; 0 // any condition returns boolean ) ( sectionBlock( text = md\"${formatUrl( \"https://example.com\", \"Overlook Hotel\" )}\\n :star: \\n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s.\" ), contextBlock( blockElements( md\"*Author:* T. M. Schwartz\", // an optional block element optionally( someParam &gt; 0 ) ( md\"*Rating:* cool\" ) ) ) ) Nested blocks blocks might contain other blocks including optional blocks: blocks ( optBlocks( latestNews.nonEmpty )( sectionBlock( text = md\"*Latest news:*\" ), dividerBlock(), latestNews.map { news =&gt; blocks( sectionBlock( text = md\" • *${news.title}*\\n${formatSlackQuoteText( news.body )}\" ), contextBlock( blockElements( md\"*Published*\", md\"${formatDate( news.published )}\" ) ) ) } ) ) Render templates to messages and views There are traits in org.latestbit.slack.morphism.client.templating to help you build your own templates: SlackBlocksTemplate - to build a general blocks template (like a home tab) SlackModalViewTemplate - to build a modal view template SlackMessageTemplate - to build a message template Define a block template in a specialised class like this: import java.time.Instant import org.latestbit.slack.morphism.client.templating._ import org.latestbit.slack.morphism.messages.SlackBlock class MyWelcomeMessageTemplateExample( userId: String ) extends SlackMessageTemplate { // All Slack messages also should provide simple plain textual representation // So this is required override def renderPlainText(): String = s\"Hey ${formatSlackUserId( userId )}\" // Blocks for our messages using DSL override def renderBlocks(): Option[List[SlackBlock]] = blocks( sectionBlock( text = md\"Hey ${formatSlackUserId( userId )}\" ), dividerBlock(), contextBlock( blockElements( md\"This is an example of block message\", md\"Current time is: ${formatDate( Instant.now(), SlackTextFormatters.SlackDateTimeFormats.DateLongPretty )}\" ) ), dividerBlock(), imageBlock( image_url = \"https://www.gstatic.com/webp/gallery3/2_webp_ll.png\", alt_text = \"Test Image\" ), actionsBlock( blockElements( button( text = pt\"Simple\", action_id = SlackActionId(\"simple-message-button\" )) ) ) ) } More examples are available here. Use them in your application code like this: val template = new MyWelcomeMessageTemplateExample( userId ) client.chat.postMessage( SlackApiChatPostMessageRequest( channel = channelId, text = template.renderPlainText(), blocks = template.renderBlocks() ) ) DSL reference Available DSL terms: blocks : list of blocks from sectionBlock : “section” dividerBlock : “divider” inputBlock : “input” contextBlock : “context” fileBlock : “file” actionsBlock : “actions” imageBlock : “image” headerBlock : “header” blockElements : list of elements of block (not all blocks support all these elements, it would be checked at the compile time): button : “button” image : “image” datePicker : “datepicker” overflow : “overflow” usersListSelect : “users_select” conversationsListSelect : “conversations_select” channelsListSelect : “channels_select” staticSelect : “static_select” externalSelect : “external_select” multiUsersListSelect : “multi_users_select” multiConversationsListSelect : “multi_conversations_select” multiChannelsListSelect : “multi_channels_select” multiStaticSelect : “multi_static_select” multiExternalSelect : “multi_external_select” radioButtons : “radio_buttons” checkboxes : “checkboxes” choiceItems : items for selects, overflow and radio buttons choiceItem : option object choiceStrItems : items for strings for users/conversations/channels selects choiceGroups : items for selects choiceGroup : option group confirm : confirm object DSL Slack text formatters You might noticed formatDate and formatUrl before. They all defined in a trait SlackTextFormatters (enable protected members in scaladoc filters to able to see them), and available for DSL templates without additional imports."
    } ,    
    {
      "title": "Slack Events API",
      "url": "/docs/events-api",
      "content": "Slack Events API To help you implement Slack Events API listener, Slack Morphism provides you: Well-typed events model and ready to use JSON encoder/decoders Slack events signature verifier, to verify requests from Slack with confidence using your signing secret Additional Web API client methods to post replies to events back Because Slack Morphism is a Web-framework agnostic, you are free to choose any you’re familiar with. However, to simplify understanding and show how to use it, Slack Morphism provides full-featured bots for Akka HTTP and http4s, which implement Events API with OAuth. Slack Events API Model There are following types of Events SlackPushEvent for all push events (like incoming messages, opening tabs, installing/uninstalling app, etc) SlackInteractionEvent for all interaction events from Slack (actions on messages and views) JSON encoders and decoders for Circe available either using import org.latestbit.slack.morphism.codecs.implicits._ or using a trait: trait MyHttpRoutesSupport extends org.latestbit.slack.morphism.codecs.CirceCodecs { ... } and you can decode all of the Slack Events like: decode[SlackInteractionEvent]( requestBody ) match { case Right( event ) =&gt; // ... case Left( ex ) =&gt; { logger.error( s\"Can't decode push event from Slack: ${ex.toString}\\n${requestBody}\" ) complete( StatusCodes.BadRequest ) } } Reply to Slack Events using response_url There are some Slack events that provide you a response_url, and you can post a message back using the specified url from those events. To help with these scenarios, you can use SlackApiClient.events.reply(), which doesn’t require any tokens to work: client.events .reply( response_url = response_url, reply = SlackApiEventMessageReply( text = template.renderPlainText(), blocks = template.renderBlocks(), response_type = Some( SlackResponseTypes.EPHEMERAL ) ) ) To find out how to create a SlackApiClient instance read this. Slack events signatures verifier To verify requests from Slack using your signing secret, there is SlackEventSignatureVerifier, which provides you verify(): To verify event request you need: A complete HTTP request body The following HTTP headers from request (header names constants): SlackEventSignatureVerifier.HttpHeaderNames.SignedTimestamp SlackEventSignatureVerifier.HttpHeaderNames.SignedHash Your signing secret from your Slack app profile val signatureVerifier = new SlackEventSignatureVerifier() signatureVerifier. verify( config.signingSecret, receivedSignedHash, receivedSignedTimestamp, requestBody ) match { case Right(success) =&gt; // the signature is verified case Left(err) =&gt; // absent or wrong signature }"
    } ,    
    {
      "title": "Getting Started",
      "url": "/docs/getting-started",
      "content": "Getting Started Add the following to your build.sbt: libraryDependencies += \"org.latestbit\" %% \"slack-morphism-client\" % \"3.1.0\" or if you’d like to start with a full-featured example and template of a Slack bot, which supports: Slack Events API, including interactivity controls, commands, home tab and shortcuts (actions) Slack Blocks in messages and modals Slack OAuth2 interaction and Slack API tokens management follow to the next sections: Start with Akka HTTP The template is using Slack-Morphism with Akka HTTP and classic scala Futures. sbt new abdolence/slack-morphism-akka-http.g8 Read more about this template here. Start with http4s The template is using Slack-Morphism in a pure functional way with http4s and cats-effect. sbt new abdolence/slack-morphism-http4s.g8 Read more about this template here."
    } ,    
    {
      "title": "Using Slack Morphism with http4s",
      "url": "/docs/http4s",
      "content": "Using Slack Morphism with http4s To show you a complete solution and give you a quick start, Slack Morphism provides you a zero-code required and complete example of a Slack bot, written with Slack Morphism and http4s. You can browse source codes of a bot example (without installing it) here. Install and configure a Slack bot from scratch Prerequisites sbt v1.3+ : build and run bot ngrok : to provide HTTPS tunnels to your dev machine for Events API) Install and run ngrok on http port Run ngrok on a port 8080: ngrok http 8080 We will need the HTTPS URL from it further, so don’t close it and open a second terminal. Create a Slack app if you don’t have any Just follow at Slack dev website and create an app profile. We will use OAuth v2/granular permissions/scopes, so: Follow to Tools → Update to Granular Scopes Select the following bot token scopes: commands im:history, im:read, im:write users:read team:read channels:history chat:write:bot (it might be also chat:write after upgrade to Granular Scopes, careful with this one) users.profile:read So, the who list would be channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read and we will use this line further. Create your own bot with sbt Choose some directory and create your own Slack bot with sbt tool: sbt new abdolence/slack-morphism-http4s.g8 Compile and run it with sbt: sbt \"run \\ --slack-app-id &lt;your-app-id&gt; \\ --slack-client-id &lt;your-client-id&gt; \\ --slack-client-secret &lt;your-client-secret&gt; \\ --slack-signing-secret &lt;your-signing-secret&gt; \\ --slack-install-bot-scope channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read \\ --slack-redirect-url https://&lt;your-ngrok-id&gt;.ngrok.io/auth/callback\" Grab all required credentials from Slack App Profile → Settings → Basic Information. If you all this correctly you should see something like this: Fill in profile parameters in your Slack app profile Features → Event Subscriptions → Enable Events Request URL: https://&lt;your-ngrok-id&gt;.ngrok.io/push Subscribe to bot events: app_home_opened, message.im, message.channels Save Changes Features → OAuth &amp; Permissions: Redirect URLs: https://&lt;your-ngrok-id&gt;.ngrok.io/auth/callback Save URLs Check if Scopes → Bot Token Scopes contains all required scopes: channels:history,chat:write,commands,im:history,im:read,im:write,team:read,users.profile:read,users:read (occasionally Slack lose something in this list after upgrading to Granular Scopes) Features → Interactive Components → Interactivity: Request URL: https://&lt;your-ngrok-id&gt;.ngrok.io/interaction Features → Home: Click on Sign Up Install a bot into your workspace using Slack OAuth v2 Just follow to the http://localhost:8080/auth/install in your browser and allow the installation. That’s it, you now have a working bot in your workspace written with Scala, Slack Morphism and http4s. Follow to the Apps section in your workspace. You should see your app with its Home tab and try to send a message to it, or click on buttons on messages and views. The code structure overview Now it is probably time to open a bot project in IDE to have deeper look how it works inside: HTTP routes are located in routes subpackage, where you can find an auxiliary trait SlackEventsMiddleware that helps you to verify Slack Events signature and work with Events API and read tokens for workspace. SlackInteractionEventsRoutes implements a handler for Slack Blocks Interactions Events (like clicking on buttons). SlackPushEventsRoutes implements a handler for Slack Push Events (like incoming messages or opening tabs). SlackOAuthRoutes implements OAuth v2 including redirection to Slack and back. For simplicity sake: This template uses an embedded database SwayDb to store tokens. For production environments, you should consider to use more appropriate solutions for your case. This template doesn’t provide any kind of HTTPS. You should consider HTTPS solutions (like reverse HTTP proxies or using http4s configs)."
    } ,    
    {
      "title": "Post Incoming Webhook messages",
      "url": "/docs/web-api/incoming-webhooks",
      "content": "Post Incoming Webhook messages You can use SlackApiClient.chat.postWebhookMessage() to post Slack Incoming Webhook messages: client.chat .postWebhookMessage( url = \"https://hooks.slack.com/services/...\", req = SlackApiPostWebHookRequest( text = template.renderPlainText(), blocks = template.renderBlocks() ) ) This method available to use without API tokens. The Webhook URL could be gather either from OAuth responses or from your Slack app profile configuration."
    } ,    
    {
      "title": "Intro",
      "url": "/docs/",
      "content": "Intro and motivations Type-safety Scala is a static-typed language, and Slack Morphism provides methods and models definitions for most of the methods and structures of Slack Web/Events API. There is a well-typed DSL to build your messages and views with Slack blocks safely as well. Easy to use While Slack Morphism uses some of FP paradigms and mostly avoids imperative style and mutable states internally, its API doesn’t expose your app anything that might make you learn additional advanced frameworks (like ZIO, cats-effects, and others). It provides you with a simple API with the support for Scala Future, Option, Either, etc. If you’re into pure functional concepts, there is support for cats-effect IO as well. Frameworks-agnostic Slack Morphism core library intentionally avoid dependencies to any additional Web-frameworks, so you free to use any framework you’re familiar with. Slack Morphism doesn’t have any dependencies to any particular HTTP client library either and uses sttp library to give you choice. However, it would be an incomplete solution to provide you only core components, and for a quick start, there are full-featured giter8 templates and examples for: Slack bot with Akka HTTP, which is implemented in a conservative way with classic Scala Futures. Slack bot with http4s, which is implemented in a pure functional way with cats-effect effects and FS2 streams. You can browse source codes of examples (without installing them) here. Non-blocking and Reactive All of the Slack Morphism API methods available in a non-blocking manner, and some of them (e.g. returning Slack channel history) also have support for reactive frameworks (Reactive Stream, FS2). Rate controlling and throttling access to Slack Web API There is ready to use a rate control/throttling implementation, which follows the Slack API rate limits and tiers (or your custom configuration). Limitations Slack Morphism doesn’t provide: RTM API (the usage of which is slowly declining in favour of Events API) Legacy Web/Events API methods and models (like Slack Message attachments, which should be replaced with Slack Blocks)"
    } ,    
    {
      "title": "Type-Safe Slack Client for Scala with Blocks Templating DSL",
      "url": "/",
      "content": ""
    } ,      
    {
      "title": "Make Web API calls",
      "url": "/docs/web-api/make-web-api-calls",
      "content": "Slack API client Choose an HTTP client backend You have to choose a sttp backend that supports scala.concurrent.Future, cats.effect.Async/Effect, monix.eval.Task response wrappers: AsyncHttpClientFutureBackend AsyncHttpClientCatsBackend AsyncHttpClientFs2Backend AsyncHttpClientMonixBackend AkkaHttpBackend OkHttpFutureBackend OkHttpMonixBackend HttpClientFutureBackend HttpClientMonixBackend Http4sBackend Add a dependency of your choice to your build.sbt. For instance, for Akka Http backend this is: \"com.softwaremill.sttp.client\" %% \"akka-http-backend\" % sttpVersion where sttpVersion should be 2.0+ Create a client to Slack Web API methods SlackApiClient provides access to all available of Slack Web API methods. Future backend // Import Slack Morphism Client import org.latestbit.slack.morphism.client._ // Import STTP backend // We're using Akka Http for this example import sttp.client.akkahttp.AkkaHttpBackend // Import support for scala.concurrent.Future implicits from cats (required if you use Future-based backend) // If you bump into compilation errors that `Future` isn't a `cats.Monad` or `cats.MonadError`, // you probably forgot to import this. // Also, you can import all implicits from cats using `import cats.implicits._` instead of this import cats.instances.future._ // Creating an STTP backend implicit val sttpBackend = AkkaHttpBackend() // this is a Future-based backend // Creating a client instance val client = SlackApiClient.create() // or similarly SlackApiClient.create[Future]() Cats Effect backend // Import Slack Morphism Client import org.latestbit.slack.morphism.client._ // Import STTP backend // We're using AsyncHttpClientCatsBackend for this example import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend // Import support for cats-effect import cats.effect._ // We should provide a ContextShift for Cats IO and STTP backend implicit val cs: ContextShift[IO] = IO.contextShift( scala.concurrent.ExecutionContext.global ) for { backend &lt;- AsyncHttpClientCatsBackend[IO]() // Creating an STTP backend client = SlackApiClient.build[IO]( backend ).create() // Create a Slack API client result &lt;- client.api.test( SlackApiTestRequest() ) // call an example method inside IO monad } yield result Monix backend // Import Slack Morphism Client import org.latestbit.slack.morphism.client._ // Import STTP backend import sttp.client.asynchttpclient.monix.AsyncHttpClientMonixBackend // Monix imports import monix.eval._ import monix.execution.Scheduler.Implicits.global for { backend &lt;- AsyncHttpClientMonixBackend() // Creating an STTP backend client = SlackApiClient.build[Task]( backend ).create() // Create a Slack API client result &lt;- client.api.test( SlackApiTestRequest() ) // call an example method inside Task monad } yield result Make Web API methods calls For most of Slack Web API methods (except for OAuth methods, Incoming Webhooks and event replies) you need a Slack token to make a call. For simple bots you can have it in your config files, or you can obtain workspace tokens using Slack OAuth. There is an example implementation of Slack OAuth v2 in Akka Http Example. Slack Morphism requires an implicit token specified as an instance of SlackApiToken: In the example below, we’re using a hardcoded Slack token, but don’t do that for your production bots and apps. You should securely and properly store all of Slack tokens. Look at Slack recommendations. import org.latestbit.slack.morphism.client._ import org.latestbit.slack.morphism.common._ import org.latestbit.slack.morphism.client.reqresp.chat._ import sttp.client.akkahttp.AkkaHttpBackend import scala.concurrent._ import cats.instances.future._ implicit val sttpBackend = AkkaHttpBackend() val client = SlackApiClient.create[Future]() implicit val slackApiToken: SlackApiToken = SlackApiBotToken(SlackAccessTokenValue(\"xoxb-89.....\")) client.chat.postMessage( SlackApiChatPostMessageRequest( channel = \"#general\", text = \"Hello Slack\" ) ) As you might noticed here, Slack Morphism API mimics Slack Web API method names, so that https://slack.com/api/chat.postMessage is client.chat.postMessage(...), or https://api.slack.com/methods/oauth.v2.access is client.oauth.v2.access(...) etc. The complete list of all of the implemented Web API methods is available here. There is also the auxiliary function client.withToken to help with implicit tokens in Scala for-comprehension: for { backend &lt;- AsyncHttpClientCatsBackend[IO]() client = SlackApiClient.build[IO]( backend ).create() result &lt;- client.withToken( yourToken )( implicit token =&gt; _.api.test( SlackApiTestRequest() ) ) } yield result Low-level HTTP API to Slack Web API In case you didn’t find a method you need on the list above, or you need something different/undocumented/legacy, there is a low-level API for this scenario: import org.latestbit.slack.morphism.client._ // Definition of your request and response as a case classes case class YourRequest(...) case class YourResponse(...) // Definitions of JSON encoder/decoders import io.circe.generic.semiauto._ implicit val yourRequestEncoder = deriveEncoder[YourRequest] implicit val yourResponseDecoder = deriveEncoder[YourRequest] // Need init a token before making calls implicit slackApiToken: SlackApiToken = ... // Make a call client.http.post[YourRequest,YourResponse]( methodUri = \"some.someMethod\", // Slack relative Method URI YourRequest() ) Please, don’t hesitate to submit a PR with model updates if you find anything missing or find model inconsistency. This project is open to help each other, so any PRs are welcomed. Avoid boilerplate making consequent non-blocking client requests with EitherT This is completely optional and just a recommendation for you. You might notice some boilerplate when you deal with consequent client requests that returns Future[ Either[ SlackApiClientError,SomeKindOfSlackResponse ] ]. To solve this in a better way, consider using an approach with EitherT[] from Cats, well described here. For example, this example shows two consequence Web API calls: We make a first async call, find some result in the first response, and getting the response result of a next async call. EitherT( slackApiClient.conversations.list( SlackApiConversationsListRequest() ) ).flatMap { channelsResp =&gt; channelsResp.channels .find( _.flags.is_general.contains( true ) ) .map { generalChannel =&gt; EitherT( client.chat.postMessage( SlackApiChatPostMessageRequest( channel = generalChannel.id, text = \"Hello\" ) ) ).map { resp =&gt; resp.ts.some } } .getOrElse( EitherT[Future, SlackApiClientError, Option[SlackTs]]( Future.successful( None.asRight ) ) ) } EitherT gives you the power to deal with those transformations and avoid boilerplate without sacrificing error handling."
    } ,    
    {
      "title": "Pagination and scrolling results",
      "url": "/docs/web-api/pagination",
      "content": "Working with pagination/batching results Some Web API methods defines cursors and pagination, to give you an ability to load a lot of data continually (using batching and continually making many requests). Examples: conversations.history conversations.list users.list … To help with those methods Slack Morphism provides additional “scroller” implementation, which deal with all scrolling/batching requests for you. With this scroller you have the following choice: Load data lazily and asynchronously with AsyncSeqIterator Load data with FS2 streams Load data with Reactive Streams Publisher[] Load data lazily, but synchronously into a standard Scala lazy container: Stream[] (for Scala 2.12) or LazyList[] (for Scala 2.13+) If none of those approaches are suitable for you, you can always use original API method. Scrollers are completely optional. For example, for conversations.history you can: Using a built-in lazy AsyncSeqIterator to scroll data Async iterator implements: foldLeft for accumulating batching results if you need it (the implementation of AsyncSeqIterator doesn’t memoize like Stream/LazyList) map to transform results foreach to iterate with effects (It is implemented as a very simple and pure functor and provides cats.Functor instance as well). This is an example of using foldLeft: client.conversations.historyScroller( SlackApiConversationsHistoryRequest( channel = \"C222....\" // some channel id ) ) .toAsyncScroller() .foldLeft( List[SlackMessage]() ) { // futureRes here is a batch result defined as Either[SlackApiClientError, List[SlackMessage]] case ( wholeList, futureRes ) =&gt; futureRes.map( wholeList ++ _ ).getOrElse( wholeList ) } Create a FS2 stream This is an optional module, and if you’re using FS2, you need additional dependency: libraryDependencies += \"org.latestbit\" %% \"slack-morphism-fs2\" % slackMorphismVer Then you can use it as following: // Additional import for support FS2 import org.latestbit.slack.morphism.client.fs2s._ client.conversations.historyScroller( SlackApiConversationsHistoryRequest( channel = \"C222....\" // some channel id ) ) .toFs2Scroller().compile.toList.unsafeRunSync() Create a reactive stream Publisher[] This is an optional module, and if you’re using Reactive Streams, you need additional dependency: libraryDependencies += \"org.latestbit\" %% \"slack-morphism-reactive-streams\" % slackMorphismVer Then you can use it as in this example: import org.reactivestreams.Publisher import cats.instances.future._ // or cats.implicits._ required import scala.concurrent.ExecutionContext.Implicits.global // Additional import for support Reactive Streams import org.latestbit.slack.morphism.client.reactive._ val publisher : Publisher[SlackMessage] = client.conversations.historyScroller( SlackApiConversationsHistoryRequest( channel = \"C222....\" // some channel id ) ) .toPublisher() // Now you can use it as any other publishers with your reactive frameworks (like Akka Streams, etc) // This is an example of Akka Streams // https://doc.akka.io/docs/akka/2.5.3/scala/stream/stream-integrations.html#integrating-with-reactive-streams import akka.stream._ import akka.stream.scaladsl._ Source .fromPublisher(publisher) .runForeach(msg =&gt; println(msg)) Load data into Stream[]/LazyList[] Because of the nature of Scala Collection API, there are limitations: this is synchronous approach and blocking operation, so when you scroll through LazyList[] there are blocking operations (at the moment of a batch loading). you won’t get any errors except for the first batch. Don’t use this for huge load scenarios, and it rather was created for testing purposes. // Synchronous approach (all batches would be loaded with blocking) client.conversations.historyScroller( SlackApiConversationsHistoryRequest( channel = \"C222....\" // some channel id ) ) .toSyncScroller( 30.seconds ) .foreach { case Right( results: LazyList[SlackMessage] ) =&gt; { // ... results are ready to scroll } case Left( err ) =&gt; // the first batch is failed here }"
    } ,    
    {
      "title": "Rate Control, throttling and retrying",
      "url": "/docs/web-api/rate-control",
      "content": "Rate control, throttling and retrying Slack API method requests Enable rate control Slack API defines rate limits to which all of your applications must follow. Slack Morphism, starting from v1.1, provides an ability to throttling your requests to control the rate limits, and delay your calls when necessary. By default, throttler isn’t enabled, so you should enable it explicitly: import org.latestbit.slack.morphism.client._ import org.latestbit.slack.morphism.client.ratectrl._ import cats.instances.future._ // Creating a client instance with throttling val client = SlackApiClient .build .withThrottler( SlackApiRateThrottler.createStandardThrottler() ) .create() The example above creates a Slack API Client that follows the official rate limits from Slack. Because the Slack rate limits apply per workspaces (separately), to use throttling and limits properly you have to specify workspace/team id in tokens: // A token should have a workspace id implicit val slackApiToken: SlackApiToken = SlackApiBotToken( \"xoxb-89.....\", workspaceId = Some( \"TS.......\" ) ) There are also different rate tiers, and all Web API methods in Slack Morphism client are marked accordingly and follow those rate tiers limits. Rate control params You can also customise rate control params using SlackApiRateThrottler.createStandardThrottler( params = ...). For example, you can set a global rate limit additionally to Slack limits using API like this: import org.latestbit.slack.morphism.client._ import org.latestbit.slack.morphism.client.ratectrl._ import scala.concurrent.duration._ import cats.instances.future._ // Defining params based on standard Slack limits val params = SlackApiRateControlParams.StandardLimits.DefaultParams.copy( globalMaxRateLimit = Some( (100, 1.minute) // 100 requests per minute ) ) // Creating a client instance with throttling val client = SlackApiClient .build .withThrottler( SlackApiRateThrottler.createStandardThrottler(params) ) .create() Enable automatic retry for rate exceeded requests To enable automatic retry of Slack Web API method requests, you need to specify maxRetries in rate control params (default value is 0): val params = SlackApiRateControlParams.StandardLimits.DefaultParams.copy( maxRetries = 3 // retry maximum 3 times ) The throttler implementation takes into account the timeout specified in an HTTP header (the Retry-After header) in Slack API rate limit response to delay your request at least that value. Using rate control parameters, you can also enable automatic retrying for other errors additionally to SlackApiRateLimitedError: val params = SlackApiRateControlParams.StandardLimits.DefaultParams.copy( maxRetries = 3 // to retry maximum 3 times, retryFor = Set( classOf[SlackApiRateLimitedError], classOf[SlackApiConnectionError] ) )"
    } ,      
    {
      "title": "Slack Web API",
      "url": "/docs/web-api",
      "content": "Slack Web API client Slack Morphism provides you with a Web API methods client, that supports: Creating a client instance and making calls Pagination and scrolling results of Slack API methods Rate control, throttling and retrying API calls Incoming Webhooks"
    }    
  ];

  idx = lunr(function () {
    this.ref("title");
    this.field("content");

    docs.forEach(function (doc) {
      this.add(doc);
    }, this);
  });

  docs.forEach(function (doc) {
    docMap.set(doc.title, doc.url);
  });
}

// The onkeypress handler for search functionality
function searchOnKeyDown(e) {
  const keyCode = e.keyCode;
  const parent = e.target.parentElement;
  const isSearchBar = e.target.id === "search-bar";
  const isSearchResult = parent ? parent.id.startsWith("result-") : false;
  const isSearchBarOrResult = isSearchBar || isSearchResult;

  if (keyCode === 40 && isSearchBarOrResult) {
    // On 'down', try to navigate down the search results
    e.preventDefault();
    e.stopPropagation();
    selectDown(e);
  } else if (keyCode === 38 && isSearchBarOrResult) {
    // On 'up', try to navigate up the search results
    e.preventDefault();
    e.stopPropagation();
    selectUp(e);
  } else if (keyCode === 27 && isSearchBarOrResult) {
    // On 'ESC', close the search dropdown
    e.preventDefault();
    e.stopPropagation();
    closeDropdownSearch(e);
  }
}

// Search is only done on key-up so that the search terms are properly propagated
function searchOnKeyUp(e) {
  // Filter out up, down, esc keys
  const keyCode = e.keyCode;
  const cannotBe = [40, 38, 27];
  const isSearchBar = e.target.id === "search-bar";
  const keyIsNotWrong = !cannotBe.includes(keyCode);
  if (isSearchBar && keyIsNotWrong) {
    // Try to run a search
    runSearch(e);
  }
}

// Move the cursor up the search list
function selectUp(e) {
  if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index) && (index > 0)) {
      const nextIndexStr = "result-" + (index - 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Move the cursor down the search list
function selectDown(e) {
  if (e.target.id === "search-bar") {
    const firstResult = document.querySelector("li[id$='result-0']");
    if (firstResult) {
      firstResult.firstChild.focus();
    }
  } else if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index)) {
      const nextIndexStr = "result-" + (index + 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Search for whatever the user has typed so far
function runSearch(e) {
  if (e.target.value === "") {
    // On empty string, remove all search results
    // Otherwise this may show all results as everything is a "match"
    applySearchResults([]);
  } else {
    const tokens = e.target.value.split(" ");
    const moddedTokens = tokens.map(function (token) {
      // "*" + token + "*"
      return token;
    })
    const searchTerm = moddedTokens.join(" ");
    const searchResults = idx.search(searchTerm);
    const mapResults = searchResults.map(function (result) {
      const resultUrl = docMap.get(result.ref);
      return { name: result.ref, url: resultUrl };
    })

    applySearchResults(mapResults);
  }

}

// After a search, modify the search dropdown to contain the search results
function applySearchResults(results) {
  const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
  if (dropdown) {
    //Remove each child
    while (dropdown.firstChild) {
      dropdown.removeChild(dropdown.firstChild);
    }

    //Add each result as an element in the list
    results.forEach(function (result, i) {
      const elem = document.createElement("li");
      elem.setAttribute("class", "dropdown-item");
      elem.setAttribute("id", "result-" + i);

      const elemLink = document.createElement("a");
      elemLink.setAttribute("title", result.name);
      elemLink.setAttribute("href", result.url);
      elemLink.setAttribute("class", "dropdown-item-link");

      const elemLinkText = document.createElement("span");
      elemLinkText.setAttribute("class", "dropdown-item-link-text");
      elemLinkText.innerHTML = result.name;

      elemLink.appendChild(elemLinkText);
      elem.appendChild(elemLink);
      dropdown.appendChild(elem);
    });
  }
}

// Close the dropdown if the user clicks (only) outside of it
function closeDropdownSearch(e) {
  // Check if where we're clicking is the search dropdown
  if (e.target.id !== "search-bar") {
    const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
    if (dropdown) {
      dropdown.classList.remove("show");
      document.documentElement.removeEventListener("click", closeDropdownSearch);
    }
  }
}
