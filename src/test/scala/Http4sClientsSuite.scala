import constants._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers

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
}
