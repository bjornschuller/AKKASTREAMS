import java.nio.file.Paths

import akka.stream.scaladsl._
import com.github.bschuller.datastream.SimpleDataStream

object Bootstrap  extends App {

  val file = Paths.get("src/main/resources/countryData.csv")
  val byteStringSource = FileIO.fromPath(file)

  val simpleDataStream = new SimpleDataStream(byteStringSource)

}

