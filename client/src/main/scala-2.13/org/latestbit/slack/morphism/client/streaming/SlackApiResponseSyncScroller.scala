package org.latestbit.slack.morphism.streaming

object SlackApiResponseSyncScroller {

  trait LazyScalaCollectionSupport[IT] {
    type SyncStreamType = Stream[IT]
  }
}
