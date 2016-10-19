package com.github.bschuller.datastream.actorrefallthewaythroughstream

import akka.actor.ActorRef
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import com.github.bschuller.datastream.actorrefallthewaythroughstream.RateOfExchangeActor.Euro
import com.github.bschuller.util.CoreServices

import scala.collection.immutable.Queue


object RateOfExchangeActor{

  case class Euro(amount: Double)
}
class RateOfExchangeActor extends ActorPublisher[(Euro,ActorRef)] with CoreServices{

  val maxBufferSize = 100

  /**
    * The requests in the buffer are not yet being processed.
    * A `Queue` has first in, first out behaviour.
    */
  var queue = Queue.empty[(Euro, ActorRef)]    // state maintained by the actor itself

  /**
    * Send the first item in the queue up the stream.
    */
  def produce() = {
    queue.headOption.foreach { nextInQueue ⇒
      queue = queue.tail
      log.info(s"Producing next item from queue (${nextInQueue}), queue size is {}", queue.size)
      onNext(nextInQueue)
    }
  }

  /**
    * You send elements to the stream by calling onNext.
    * You are allowed to send as many elements as have been requested by the stream subscriber.
    * This amount can be inquired with totalDemand (i.e., total number of requested elements from the stream subscriber).
    * It is only allowed to use onNext when isActive and totalDemand>0, otherwise onNext will throw IllegalStateException.
    *
    */
  override def receive: Receive = {

    // Internal request: This message is delivered to the [[ActorPublisher]] actor when the stream subscriber requests more messages
    case Request(demand) =>
      system.log.info(s"Internal request from stream subscriber")
      0 until demand.toInt foreach(demand => produce())

      /**
        * External request: are pushed to the stream when totalDemand > 0 otherwise it is stored in the Queue.
        * You are storing the Sender Actor ref because you need that address to send the message back to
        */
    case euro: Euro if totalDemand == 0 => {
      if(queue.length < maxBufferSize){
        queue = queue :+ (euro, sender) // when changing sender to self it sends message to itself which sends it back in the stream
        system.log.info(s"Accepted external request, queue size is ${queue.size}. Placing value ${euro} in queue.")
      } else{
        log.info(s"Buffer length exceeds $maxBufferSize. Dropping value ${euro.amount}.")
      }
    }
    case euro: Euro if isActive && totalDemand > 0 =>{
      system.log.info(s"SENDING ACTOR REF ==> ${sender}")
      onNext((euro, sender)) // when changing sender to self it sends message to itself which sends it back in the stream
    }
  }
}

