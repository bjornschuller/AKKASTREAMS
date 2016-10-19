package com.github.bschuller.datastream.actorbasedstream

import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Request
import com.github.bschuller.datastream.actorbasedstream.ActorBasedSourcePublisher.SimpleMessage
import com.github.bschuller.util.CoreServices

import scala.collection.immutable.Queue

/**
  * At a high level all our Publisher is doing pushing SimpleMessages to the stream based on demand from a subscriber.
  */

object ActorBasedSourcePublisher{
  case class SimpleMessage(number: Int)
}

class ActorBasedSourcePublisher extends ActorPublisher[SimpleMessage] with CoreServices{

  val maxBufferSize = 100

  /**
    * The requests in the buffer are not yet being processed.
    * A `Queue` has first in, first out behaviour.
    */
  var queue = Queue.empty[SimpleMessage]    // state maintained by the actor itself

  /**
    * Send the first item in the queue up the stream.
    */
  def produce() = {
    queue.headOption.foreach { nextInQueue ⇒
      queue = queue.tail
      log.info(s"Producing next item from queue (value: ${nextInQueue.number}, queue size is {}", queue.size)
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

    // External request: are pushed to the stream when totalDemand > 0 otherwise it is stored in the Queue.
    case sm: SimpleMessage if totalDemand == 0 => {
      if(queue.length < maxBufferSize){
        queue = queue :+ sm
        system.log.info(s"Accepted external request, queue size is ${queue.size}. Placing value ${sm.number} in queue.")
      } else{
        log.info(s"Buffer length exceeds $maxBufferSize. Dropping value ${sm.number}.")
      }
    }
    case sm: SimpleMessage if isActive && totalDemand > 0 =>{
      system.log.info(s"A. SOURCE ==> EXTERNAL REQUEST TO PUSH A VALUE OF ${sm.number} TO STREAM")
      onNext(sm)
    }
  }

}


