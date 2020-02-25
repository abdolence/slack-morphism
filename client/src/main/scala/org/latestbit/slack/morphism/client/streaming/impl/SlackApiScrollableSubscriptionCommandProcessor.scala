/*
 * Copyright 2019 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.latestbit.slack.morphism.client.streaming.impl

import org.latestbit.slack.morphism.client.SlackApiClientError
import org.latestbit.slack.morphism.client.streaming.SlackApiResponseScroller
import org.reactivestreams.Subscriber

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class SlackApiScrollableSubscriptionCommandProcessor[IT, PT](
    subscriber: Subscriber[_ >: IT],
    scrollableResponse: SlackApiResponseScroller[IT, PT],
    maxItems: Option[Long] = None
) {
  import SlackApiScrollableSubscriptionCommandProcessor._

  @volatile private var subscriptionLastResponse = Option( scrollableResponse.first() )
  @volatile private var lastItems: Iterable[IT] = List.empty
  @volatile private var sent: Long = 0
  @volatile private var started: Boolean = false
  @volatile private var active: Boolean = false
  @volatile private var commandBuffer: Vector[SlackApiScrollableSubscriptionCommand] = Vector()

  private def nextBatch()(
      implicit ec: ExecutionContext
  ): Future[Either[SlackApiClientError, Iterable[IT]]] = {

    Option( lastItems )
      .filter( _.nonEmpty )
      .map(items => Future.successful( Right( items ) ) )
      .orElse(
        subscriptionLastResponse
          .map { futureResp =>
            futureResp.andThen {
              case Success( res ) => {
                res match {
                  case Right( successResp ) => {
                    lastItems = successResp.items
                    subscriptionLastResponse = successResp.getLatestPos.map( scrollableResponse.next )
                  }
                  case Left( _ ) => {
                    lastItems = List()
                    subscriptionLastResponse = None
                  }
                }
              }
              case Failure( _ ) => {
                subscriptionLastResponse = None
              }
            }
          }
          .map( _.map( _.map( _.items ) ) )
      )
      .getOrElse(
        Future.successful( Right( List() ) )
      )
  }

  private def pumpNextBatch( reqN: Long )(
      implicit ec: ExecutionContext
  ): Unit = {
    if (reqN > 0) {
      nextBatch() onComplete {
        case Success( response ) => {
          val n = maxItems.filter( _ < sent + reqN ).map( _ - sent ).getOrElse( reqN )
          if (n > 0 && started) {
            response match {
              case Right( items ) if items.isEmpty => {
                subscriber.onComplete()
              }
              case Right( items ) => {

                val ( toSend, toBuffer ) = items.splitAt( n.toInt )
                lastItems = toBuffer
                toSend.foreach { item =>
                  subscriber.onNext( item )
                  sent = sent + 1
                }
                maxItems match {
                  case Some( maxItemsValue ) if maxItemsValue <= sent => {
                    subscriber.onComplete()
                  }
                  case _ => {
                    if (toSend.size < n)
                      pumpNextBatch( n - toSend.size )
                    else {
                      var hasCmd: Option[SlackApiScrollableSubscriptionCommand] = None
                      synchronized {
                        commandBuffer.headOption match {
                          case Some( cmd ) => {
                            hasCmd = Some( cmd )
                            commandBuffer = commandBuffer.drop( 1 )
                          }
                          case _ => {
                            active = false
                          }
                        }
                      }

                      if (hasCmd.nonEmpty) {
                        hasCmd.foreach( executeCommand )
                      }
                    }
                  }
                }

              }
              case Left( err ) => subscriber.onError( err )
            }
          }
        }
        case Failure( ex ) => subscriber.onError( ex )
      }
    }

  }

  private def executeCommand( cmd: SlackApiScrollableSubscriptionCommand )(
      implicit ec: ExecutionContext
  ): Unit = {
    cmd match {
      case RequestElements( n ) => {
        pumpNextBatch( n )
      }
    }
  }

  def enqueueCommand( cmd: SlackApiScrollableSubscriptionCommand )(
      implicit ec: ExecutionContext
  ): Unit = {
    var needThread = false

    synchronized {
      if (!active && started) {
        needThread = true
        active = true
      } else {
        commandBuffer = commandBuffer :+ cmd
      }
    }

    if (needThread) {
      val _ = Future {
        executeCommand( cmd )
      }
    }

  }

  def start()(
      implicit ec: ExecutionContext
  ): Unit = {

    var hasCmd: Option[SlackApiScrollableSubscriptionCommand] = None
    synchronized {
      commandBuffer.headOption match {
        case Some( cmd ) => {
          hasCmd = Some( cmd )
          commandBuffer = commandBuffer.drop( 1 )
        }
        case _ => None
      }
    }

    if (hasCmd.nonEmpty) {
      synchronized {
        active = true
        started = true
      }

      hasCmd.foreach { cmd =>
        Future {
          executeCommand( cmd )
        }
      }
    } else {
      started = true
    }
  }

  def shutdown(): Unit = {
    synchronized {
      started = false
      commandBuffer = Vector()
    }
  }

}

object SlackApiScrollableSubscriptionCommandProcessor {

  sealed trait SlackApiScrollableSubscriptionCommand
  case class RequestElements( n: Long ) extends SlackApiScrollableSubscriptionCommand

}
