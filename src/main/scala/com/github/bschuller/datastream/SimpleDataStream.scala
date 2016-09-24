package com.github.bschuller.datastream

import akka.stream.IOResult
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.github.bschuller.util.CoreServices

import scala.concurrent.Future

/**
  * Simple datastream that transforms the byteString source to utf8Strings and prints the output to the console
  */
class SimpleDataStream(byteStringSource: Source[ByteString, Future[IOResult]]) extends CoreServices{

  val transformToUtf8StringFlow = Flow[ByteString].map{response => response.utf8String}

  byteStringSource.via(transformToUtf8StringFlow).runWith(Sink.foreach(x => println(x)))

}
