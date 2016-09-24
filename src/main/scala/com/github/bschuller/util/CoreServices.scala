package com.github.bschuller.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext


trait CoreServices {
  implicit val system: ActorSystem =  ActorSystem("ActorSystem")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val log = akka.event.Logging.getLogger(system, this)
}
