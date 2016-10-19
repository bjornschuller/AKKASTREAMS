package com.github.bschuller.datastream.actorrefallthewaythroughstream

import akka.actor.{ActorRef, Props}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.bschuller.datastream.actorrefallthewaythroughstream.RateOfExchangeActor.Euro
import com.github.bschuller.util.CoreServices


class ActorRefBasedStream extends CoreServices{

  /**
    * Creates a `Source` that is materialized to an [[akka.actor.ActorRef]] which points to an Actor
    */
  val actorSource = Source.actorPublisher[(Euro, ActorRef)](Props[RateOfExchangeActor])

  protected lazy val sink = Sink.foreach[(Euro, ActorRef)]{
    case (response, ref) ⇒
      println(s"D. SINK ==> ${response}")
      ref ! response

  }

  val convertToUSD = Flow[(Euro, ActorRef)].map{
    case (euro, ref) =>
      system.log.info(s"PUBLISH ACTOR REF ==> $publishActor AND SENDING ACTOR REF ==> ${ref} ")
      (euro.copy(euro.amount * 1.1), ref)
  }


  /**
    * Build the stream, run it and get a reference to the ActorBasedSourcePublisher.
    * The runWith method returns a value that is materialized value
    * of the `Source`
    */
  lazy val publishActor: ActorRef = convertToUSD to sink runWith actorSource

}
