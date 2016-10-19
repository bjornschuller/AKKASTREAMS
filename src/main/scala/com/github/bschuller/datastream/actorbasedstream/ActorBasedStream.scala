package com.github.bschuller.datastream.actorbasedstream

import akka.actor.{ActorRef, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.bschuller.datastream.actorbasedstream.ActorBasedSourcePublisher.SimpleMessage
import com.github.bschuller.util.CoreServices



class ActorBasedStream extends CoreServices {

  /**
    * Creates a `Source` that is materialized to an [[akka.actor.ActorRef]] which points to an Actor
    */
  val actorBasedSource = Source.actorPublisher[SimpleMessage](Props[ActorBasedSourcePublisher])

  protected lazy val sink = Sink.foreach[SimpleMessage]{
      case (response) ⇒
        println(s"D. SINK ==> ${response.number}")
    }

    val multiplyingBy2Flow = Flow[SimpleMessage].map{
      case (message) =>
        println(s"B. MULTIPLYBY2FLOW ==> ${message.number} * 2")
        val multipliedMessage = message.copy(message.number * 2)
        multipliedMessage
    }

  val add1AndDelayFlow = Flow[SimpleMessage].map{
    case (message) =>
      println(s"C. ADDONANDDELAYFLOW ==> ${message.number} + 1")
      Thread.sleep(19)
      val plusOne = message.copy(message.number + 1)
      plusOne
  }

  /**
    * Build the stream, run it and get a reference to the ActorBasedSourcePublisher.
    * The runWith method returns a value that is materialized value
    * of the `Source`
    */
  lazy val publishActor: ActorRef = (multiplyingBy2Flow via add1AndDelayFlow) to sink runWith actorBasedSource



}
