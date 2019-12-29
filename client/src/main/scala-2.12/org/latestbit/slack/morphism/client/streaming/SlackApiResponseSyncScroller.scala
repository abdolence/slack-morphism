package org.latestbit.slack.morphism.client.streaming

object SlackApiResponseSyncScroller {
    trait LazyScalaCollectionSupport[IT] {
        type SyncStreamType = Stream[IT]
    }
}