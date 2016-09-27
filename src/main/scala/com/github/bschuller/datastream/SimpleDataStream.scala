package com.github.bschuller.datastream

import akka.stream.IOResult
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.util.ByteString
import com.github.bschuller.util.CoreServices

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

/**
  * Simple datastream that transforms the byteString source to utf8Strings and prints the output to the console
  */
class SimpleDataStream(byteStringSource: Source[ByteString, Future[IOResult]]) extends CoreServices{
  println(s" bytestingr --> $byteStringSource")

  val reorganizeDataByLine = Framing.delimiter(ByteString("\n"), Int.MaxValue, false)

  val convertLineToList = Flow[ByteString].map{
    byteString =>
      val line = byteString.utf8String
      separator(line)
  }

  val printReport = Flow[Map[String, Option[String]]].map{
    dataMapa =>
      println(s"=====${dataMapa("Country").getOrElse("Unknown Country")} Country Data Report=====")
      for ((key,value) <- dataMapa) {
        println(s"key: $key, value: $value")
      }
      println("\n\n")
  }

  val sink = Sink.ignore

  val graph = byteStringSource.
    via(reorganizeDataByLine).
    via(convertLineToList).
    via(printReport).
    to(sink)

  graph.run()



  private def separator(line: String): Map[String, Option[String]] = {
    val lineValueList = new ListBuffer[String]()
    line.split(";").foreach{
      value => lineValueList += value
    }
    Map(
      "Country" -> lineValueList.headOption,
      "Area" -> lineValueList.lift(1),
      "Birth rate/1000 population" -> lineValueList.lift(2),
      "Account balance" -> lineValueList.lift(3),
      "Death rate/1000 population" -> lineValueList.lift(4),
      "Exports" -> lineValueList.lift(8),
      "HIV deaths" -> lineValueList.lift(13),
      "HIV living" -> lineValueList.lift(14),
      "Internet users" -> lineValueList.lift(21),
      "Life expectancy at birth(years)" -> lineValueList.lift(24),
      "Population" -> lineValueList.lift(37),
      "Unemployment rate(%)" -> lineValueList.lift(44)
    )
  }

}
