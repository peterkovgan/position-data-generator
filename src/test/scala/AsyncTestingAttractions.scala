import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import core.AttractionActor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.LoggerFactory

class AsyncTestingAttractions extends TestKit(ActorSystem("OfficeTest"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll{

  val logger = LoggerFactory.getLogger("test.AsyncTestingAttractions")

  "An AttractionPoint actor" must {
      "send back location configuration value" in {
        val office = system.actorOf(AttractionActor.props(1,0,0, null))
        office ! "location"
        expectMsg("12,40")
      }
  }



}