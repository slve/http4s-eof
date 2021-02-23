import constants._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers

class SoftwareMillClientsSuite extends AnyFeatureSpec with Matchers {

  Feature("Sttp client") {
    Scenario("Post request small payload size") {
      val app = new SttpClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new SttpClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

}


