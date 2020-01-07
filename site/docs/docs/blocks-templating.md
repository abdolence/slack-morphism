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
As you can see it just a JSON, let's look at how it might look type-safe DSL with Slack Morphism Blocks DSL:

```scala
blocks(
    sectionBlock(
      text = md"A message *with some bold text* and _some italicized text_."
    )
)
```

Slack Morphism Blocks DSL not just give you a DSL and function to create a JSON from this, but also provides 
both compile time and run time checking for structure and formatting rules for Slack Blocks, 
using Scala language abilities and [Design by cntract (DbC)](https://en.wikipedia.org/wiki/Design_by_contract) principles.

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
    * `radioButtons` : ["radio_buttons"](https://api.slack.com/reference/block-kit/block-elements#radio_buttons)
* `choiceItems` : items for selects, overflow and radio buttons
    * `choiceItem` : [option object](https://api.slack.com/reference/block-kit/composition-objects#option)
* `choiceStrItems` : items for strings for users/conversations/channels selects    
* `choiceGroups` : items for selects
    * `choiceGroup` : [option group](https://api.slack.com/reference/block-kit/composition-objects#option_group)
* `confirm` : [confirm object](https://api.slack.com/reference/block-kit/composition-objects#confirm)

### Text object formatting

There are specialised DSL string interpolators:
* `md"Mark down Text"` : creates Slack `mrkdwn` text object 
* `plain"Plain text"`  : creates Slack `plain` text object

### Option blocks and elements

There are very useful DSL terms to provide an optional block, element, field or a choice item 
depends on some user defined condition:

* `optBlocks`
* `optBlock`
* `optBlockEl`
* `optSectionField`
* `optChoiceItem`
* `optChoiceGroup`  

They all lazily evaluated second parameter, so it might be useful as well.

You can use them in Scala as:

```scala
// Conditional item of overflow menu:
sectionBlock(
  text = md"Test 2",
  accessory = overflow(
    action_id = "-",
    options = choiceItems(
      choiceItem( text = plain"test-menu-item", value = "" ),
      optChoiceItem( someUserParam > 0 ) ( choiceItem( text = plain"conditional-menu-item", value = "" ) )
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
            md"*Author:* T. M. Schwartz"
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


### Rendering blocks to messages and views

There are traits in `org.latestbit.slack.morphism.client.templating` to help you build your own templates:
* `SlackBlocksTemplate` - to build a general blocks template (like a home tab)
* `SlackModalViewTemplate` - to build a modal view template
* `SlackMessageTemplate` - to build a message template

1. Define your template in a specialised class like this:
```scala

import java.time.Instant

import org.latestbit.slack.morphism.client.templating._
import org.latestbit.slack.morphism.messages.SlackBlock

class SlackWelcomeMessageTemplateExample( userId: String ) extends SlackMessageTemplate {

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
          md"Current time is: ${formatDate( Instant.now(), SlackTextFormatters.SlackDateTimeFormats.DATE_LONG_PRETTY )}"
        )
      ),
      dividerBlock(),
      imageBlock( image_url = "https://www.gstatic.com/webp/gallery3/2_webp_ll.png", alt_text = "Test Image" ),
      actionsBlock(
        blockElements(
          button( text = plain"Simple", action_id = "simple-message-button" )
        )
      )
    )

}
```

2. Use them in your application code like this:

```scala
val template = new SlackWelcomeMessageTemplateExample( userId )
slackApiClient.chat
.postMessage(
  SlackApiChatPostMessageRequest(
    channel = channelId,
    text = template.renderPlainText(),
    blocks = template.renderBlocks()
  )
)
```

### DSL Slack text formatters

You might noticed `formatDate` and `formatUrl` before.
They all defined in a trait [SlackTextFormatters](/org/latestbit/slack/morphism/client/templating/SlackTextFormatters.html)
and available for DSL templates without additional imports.
