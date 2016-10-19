import akka.actor.PoisonPill
import com.github.bschuller.datastream.actorbasedstream.ActorBasedStream
import com.github.bschuller.util.CoreServices
import com.github.bschuller.datastream.actorbasedstream.ActorBasedSourcePublisher.SimpleMessage
import akka.pattern.ask

class ActorBasedStreamTest extends TestSpec with CoreServices{

  feature("Testing the ActorBasedStream") {

    val stream = new ActorBasedStream()
    val actor = stream.publishActor

    scenario("1. Sending a message to the ActorBasedSourcePublisher") {
//      (actor ? SimpleMessage(1)).futureValue shouldBe 3 // UNABLE TO DO THIS SINCE ActorRef is not pushed through the stream

      for( a <- 0 to 1000){
        Thread.sleep(14)
        actor ! SimpleMessage(number = a)
      }

      actor ! PoisonPill
    }
  }
}
