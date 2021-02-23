import constants._
import constants.jdkhttp._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers

class MagicNumbersJdkHttp extends AnyFeatureSpec with Matchers {

  Feature("JdkHttp client") {
    Scenario("Is always broken at first request, when req = magic AND res = magic") {
      val app = new JdkHttpClientTest(short, magicRequestPayloadSize, magicResponsePayloadSize)
      an[java.io.IOException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Tends to hold on a longer run, when req = magic while res is small") {
      val app = new JdkHttpClientTest(long, magicRequestPayloadSize, `1kB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Tends to hold on a longer run, when req is small while res = magic") {
      val app = new JdkHttpClientTest(long, `1kB`, magicResponsePayloadSize)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Tends to hold on a longer run, when req = (magic - 1) AND res = (magic - 1)") {
      val app = new JdkHttpClientTest(long, magicRequestPayloadSize - 1, magicResponsePayloadSize - 1)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }
}
