import constants._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import zio.Runtime

import scala.concurrent.duration.DurationInt

class Http4sClientsSuite extends AnyFeatureSpec with Matchers {

  Feature("Async Http client") {
    Scenario("Post request small payload size") {
      val app = new AsyncHttpClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new AsyncHttpClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("Blaze client") {
    Scenario("Post request small payload size") {
      val app = new BlazeClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new BlazeClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("Ember client") {
    Scenario("Post request small payload size") {
      val app = new EmberClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new EmberClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("JDK Http client") {
    Scenario("Post request small payload size") {
      val app = new JdkHttpClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new JdkHttpClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size for an extended period") {
      val app = new JdkHttpClientTest(long, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("Jetty client") {
    Scenario("Post request small payload size") {
      val app = new JettyClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new JettyClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("OkHttp client") {
    Scenario("Post request small payload size") {
      val app = new OkHttpClientTest(short, `1kB`, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new OkHttpClientTest(short, `1MB`, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }

  Feature("ZIO + http4s client") {
    Scenario("Post request small payload size") {
      val app = new HttpZioClientTest(short, `1kB`, `1kB`)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request magic payload size 0 0") {
      val app = new HttpZioClientTest(10.seconds, blaze.magicRequestPayloadSize, blaze.magicResponsePayloadSize)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request magic payload size -1 0") {
      val app = new HttpZioClientTest(10.seconds, blaze.magicRequestPayloadSize - 1, blaze.magicResponsePayloadSize)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request magic payload size 0 -1") {
      val app = new HttpZioClientTest(10.seconds, blaze.magicRequestPayloadSize, blaze.magicResponsePayloadSize - 1)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request large payload size") {
      val app = new HttpZioClientTest(short, `1MB` / 10, `1MB` / 10)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }

    Scenario("Post request huge payload size") {
      val app = new HttpZioClientTest(short, 100 * `1MB`, 100 * `1MB`)
      Runtime.default.unsafeRun(app.run(List())).code mustBe 0
    }
  }
}
