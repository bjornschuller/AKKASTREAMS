import akka.testkit.TestProbe
import com.github.bschuller.datastream.actorrefallthewaythroughstream.ActorRefBasedStream
import com.github.bschuller.datastream.actorrefallthewaythroughstream.RateOfExchangeActor.Euro
import com.github.bschuller.util.CoreServices
import akka.pattern.ask

class ActorRefStreamTest extends TestSpec with CoreServices{

  feature("Testing the ActorBasedStream") {

    val stream = new ActorRefBasedStream().publishActor // creates an actorRef of the stream
    val probe = TestProbe()

    scenario("1. Sending a message to the RateOfExchangeActor") {
      probe.send(stream, Euro(12.00))   // here the probe is the sender

      probe.expectMsg(Euro(13.200000000000001))
    }
    scenario("2. Sending a message to the RateOfExchangeActor") {
      val response = (stream ? Euro(12.00)).futureValue   // an ? creates another actorRef under the hood that is the sender Actor (ask pattern is not a fire and forget style)
      response shouldBe Euro(13.200000000000001)
    }
  }
}