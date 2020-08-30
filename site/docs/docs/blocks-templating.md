---
layout: edocs
title: Slack Events API
permalink: docs/templating
---
# Slack Blocks Templating DSL
Simple to use and type-safe DSL for Slack Blocks.

## Intro
Slack Blocks are visual components that can be stacked and arranged to create app layouts in messages and views. 
Read [official docs](https://api.slack.com/block-kit/building) for introduction to Slack Block Kit.
             
Let's take some very simple block example:

```json
{
  "blocks": [
      {
        "type": "section",
        "text": {
            "type": "mrkdwn",
            "text": "A message *with some bold text* and _some italicized text_."
        }
      }
  ]
}
```
Now, let's look at how it might look with type-safe Scala code using Slack Morphism Blocks DSL:

```scala
blocks(
    sectionBlock(
      text = md"A message *with some bold text* and _some italicized text_."
    )
)
```

Using Slack Morphism Blocks DSL you can embed those definitions into 
your Slack messages and views, and DSL also provides you both compile time and runtime 
validations for structure and formatting rules defined for Slack Blocks, 
using Scala language features and [Design by contract (DbC)](https://en.wikipedia.org/wiki/Design_by_contract) principles.

Let's look at another more complex example:

Json:
```json
{
  "blocks": [
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "<https://example.com|Overlook Hotel> \n :star: \n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s."
      },
      "accessory": {
        "type": "image",
        "image_url": "https://is5-ssl.mzstatic.com/image/thumb/Purple3/v4/d3/72/5c/d3725c8f-c642-5d69-1904-aa36e4297885/source/256x256bb.jpg",
        "alt_text": "Haunted hotel image"
      }
    },
    {
      "type": "section",
      "fields": [
        {
          "type": "mrkdwn",
          "text": "*Average Rating*\n1.0"
        },
        {
          "type": "mrkdwn",
          "text": "*Updated*\n<!date^1578400713^{date_pretty}|07 Jan 2020 12:38:33 GMT>"
        }
      ]
    },
    {
      "type": "context",
      "elements": [
        {
          "type": "mrkdwn",
          "text": "*Author:* T. M. Schwartz"
        }
      ]
    }
  ]
}
```

It would be using DSL defined as:

```scala
blocks(
    sectionBlock(
      text =
        md"${formatUrl( "https://example.com", "Overlook Hotel" )}\n :star: \n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s."
    ),
    sectionBlock(
      fields =
        sectionFields(
          md"*Average Rating*\n1.0",
          md"*Updated*\n${formatDate(Instant.now())}"
        )
    ),
    contextBlock(
        blockElements(
            md"*Author:* T. M. Schwartz"
        )
    )
)
```


### Text object formatting

There are specialised DSL string interpolators:
* `md"Mark down Text"` : creates Slack `mrkdwn` text object 
* `pt"Plain text"`  : creates Slack `plain` text object

### Optional blocks and elements

There are very useful DSL terms to provide an optional block, element, field or a choice item 
depends on some user defined condition (a predicate):

* `optionally` - single optional item (block, block element, section field, choice item, choice group)
* `optBlocks` - optional list of blocks

The `optionally` function evaluates lazily its second parameter, so it might be useful as well to know.

You can use them in Scala as:

```scala
// Conditional item of overflow menu:
sectionBlock(
  text = md"Test 2",
  accessory = overflow(
    action_id = SlackActionId("-"),
    options = choiceItems(
      choiceItem( text = pt"test-menu-item 1", value = "1" ),
      choiceItem( text = pt"test-menu-item 2", value = "2" ),
      optionally( someUserParam > 0 ) ( choiceItem( text = pt"conditional-menu-item 3", value = "3" ) )
    )
  )
)
```

```scala
// Conditional group of blocks example
optBlocks(
    someParam > 0 // any condition returns boolean
) (
    sectionBlock(
      text =
        md"${formatUrl( "https://example.com", "Overlook Hotel" )}\n :star: \n Doors had too many axe holes, guest in room 237 was far too rowdy, whole place felt stuck in the 1920s."
    ),
    contextBlock(
        blockElements(
            md"*Author:* T. M. Schwartz",
            // an optional block element
            optionally( someParam > 0 ) ( md"*Rating:* cool" ) 
        )
    )
)
```

### Nested blocks
`blocks` might contain other `blocks` including optional blocks:

```scala
blocks (
    optBlocks( latestNews.nonEmpty )(
        sectionBlock(
          text = md"*Latest news:*"
        ),
        dividerBlock(),
        latestNews.map { news =>
          blocks(
            sectionBlock(
              text = md" • *${news.title}*\n${formatSlackQuoteText( news.body )}"
            ),
            contextBlock(
              blockElements(
                md"*Published*",
                md"${formatDate( news.published )}"
              )
            )
          )
        }
    )
)
```


### Render templates to messages and views

There are traits in `org.latestbit.slack.morphism.client.templating` to help you build your own templates:
* `SlackBlocksTemplate` - to build a general blocks template (like a home tab)
* `SlackModalViewTemplate` - to build a modal view template
* `SlackMessageTemplate` - to build a message template

#### Define a block template in a specialised class like this:

```scala
import java.time.Instant

import org.latestbit.slack.morphism.client.templating._
import org.latestbit.slack.morphism.messages.SlackBlock

class MyWelcomeMessageTemplateExample( userId: String ) extends SlackMessageTemplate {

 // All Slack messages also should provide simple plain textual representation
 // So this is required 
  override def renderPlainText(): String =
    s"Hey ${formatSlackUserId( userId )}"

  // Blocks for our messages using DSL 
  override def renderBlocks(): Option[List[SlackBlock]] =
    blocks(
      sectionBlock(
        text = md"Hey ${formatSlackUserId( userId )}"
      ),
      dividerBlock(),
      contextBlock(
        blockElements(
          md"This is an example of block message",
          md"Current time is: ${formatDate( Instant.now(), SlackTextFormatters.SlackDateTimeFormats.DateLongPretty )}"
        )
      ),
      dividerBlock(),
      imageBlock( image_url = "https://www.gstatic.com/webp/gallery3/2_webp_ll.png", alt_text = "Test Image" ),
      actionsBlock(
        blockElements(
          button( text = pt"Simple", action_id = SlackActionId("simple-message-button" ))
        )
      )
    )
}
```
More examples are available [here](https://github.com/abdolence/slack-morphism/tree/master/examples/akka-http/src/main/scala/org/latestbit/slack/morphism/examples/akka/templates).

#### Use them in your application code like this:

```scala
val template = new MyWelcomeMessageTemplateExample( userId )
client.chat.postMessage(
  SlackApiChatPostMessageRequest(
    channel = channelId,
    text = template.renderPlainText(),
    blocks = template.renderBlocks()
  )
)
```

## DSL reference
Available DSL terms:

* `blocks` : list of blocks from
    * `sectionBlock` : ["section"](https://api.slack.com/reference/block-kit/blocks#section)
    * `dividerBlock` : ["divider"](https://api.slack.com/reference/block-kit/blocks#divider)
    * `inputBlock` : ["input"](https://api.slack.com/reference/block-kit/blocks#input)
    * `contextBlock` : ["context"](https://api.slack.com/reference/block-kit/blocks#context)
    * `fileBlock` : ["file"](https://api.slack.com/reference/block-kit/blocks#file)
    * `actionsBlock` : ["actions"](https://api.slack.com/reference/block-kit/blocks#actions)
    * `imageBlock` : ["image"](https://api.slack.com/reference/block-kit/blocks#image)
    * `headerBlock` : ["header"](https://api.slack.com/reference/block-kit/blocks#header)
* `blockElements` : list of elements of block (not all blocks support all these elements, it would be checked at the compile time):
    * `button` : ["button"](https://api.slack.com/reference/block-kit/block-elements#button)
    * `image` : ["image"](https://api.slack.com/reference/block-kit/block-elements#image)
    * `datePicker` : ["datepicker"](https://api.slack.com/reference/block-kit/block-elements#datepicker)
    * `overflow` : ["overflow"](https://api.slack.com/reference/block-kit/block-elements#overflow)
    * `usersListSelect` : ["users_select"](https://api.slack.com/reference/block-kit/block-elements#datepicker)
    * `conversationsListSelect` : ["conversations_select"](https://api.slack.com/reference/block-kit/block-elements#conversations_select)
    * `channelsListSelect` : ["channels_select"](https://api.slack.com/reference/block-kit/block-elements#channels_select)
    * `staticSelect` : ["static_select"](https://api.slack.com/reference/block-kit/block-elements#select)
    * `externalSelect` : ["external_select"](https://api.slack.com/reference/block-kit/block-elements#external_select)
    * `multiUsersListSelect` : ["multi_users_select"](https://api.slack.com/reference/block-kit/block-elements#multi_users_select)
    * `multiConversationsListSelect` : ["multi_conversations_select"](https://api.slack.com/reference/block-kit/block-elements#multi_conversations_select)
    * `multiChannelsListSelect` : ["multi_channels_select"](https://api.slack.com/reference/block-kit/block-elements#multi_channels_select)
    * `multiStaticSelect` : ["multi_static_select"](https://api.slack.com/reference/block-kit/block-elements#multi_select)
    * `multiExternalSelect` : ["multi_external_select"](https://api.slack.com/reference/block-kit/block-elements#multi_external_select)
    * `radioButtons` : ["radio_buttons"](https://api.slack.com/reference/block-kit/block-elements#radio)
    * `checkboxes` : ["checkboxes"](https://api.slack.com/reference/block-kit/block-elements#checkboxes)    
* `choiceItems` : items for selects, overflow and radio buttons
    * `choiceItem` : [option object](https://api.slack.com/reference/block-kit/composition-objects#option)
* `choiceStrItems` : items for strings for users/conversations/channels selects    
* `choiceGroups` : items for selects
    * `choiceGroup` : [option group](https://api.slack.com/reference/block-kit/composition-objects#option_group)
* `confirm` : [confirm object](https://api.slack.com/reference/block-kit/composition-objects#confirm)

### DSL Slack text formatters

You might noticed `formatDate` and `formatUrl` before.
They all defined in a trait [SlackTextFormatters](/api/org/latestbit/slack/morphism/client/templating/SlackTextFormatters.html)
 (enable protected members in scaladoc filters to able to see them), and available for DSL templates without additional imports.

