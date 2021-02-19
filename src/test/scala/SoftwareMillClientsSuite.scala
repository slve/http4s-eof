import constants._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import zio.Runtime

import scala.concurrent.duration.DurationInt

class SoftwareMillClientsSuite extends AnyFeatureSpec with Matchers {
  Feature("Sttp client") {
    Scenario("Post request small payload size") {
      val app = new SttpClientTest(long, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new SttpClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("ZIO + sttp client") {
    Scenario("Post request small payload size") {
      val app = new SttpZioClientTest(5.seconds, 81 *`1kB`, 81 *`1kB`)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new SttpZioClientTest(short, `1MB`, `1MB`)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request large payload size for an extended period") {
      val app = new SttpZioClientTest(long, `1MB`, `1MB`)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request extra large payload size for an extended period") {
      val app = new SttpZioClientTest(long, 20 * `1MB`, 20 * `1MB`)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }
  }

}
