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
    )
)
```
