import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import org.scalatest._

class TestSpec extends FeatureSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with ScalaFutures{
  implicit val timeout = Timeout(10 seconds)
}
