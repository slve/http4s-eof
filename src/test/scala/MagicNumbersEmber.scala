import constants._
import constants.ember._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers

class MagicNumbersEmber extends AnyFeatureSpec with Matchers {

  Feature("Ember client") {
    Scenario("Is always broken at first request, when req = magic even if res is large") {
      val app = new EmberClientTest(short, magicRequestPayloadSize, `1MB`)
      an[java.io.IOException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Is always broken at first request, when req = magic even if res is small") {
      val app = new EmberClientTest(short, magicRequestPayloadSize, `1kB`)
      an[java.io.IOException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Tends to hold for short period, when req = (magic - 1) even if res is large") {
      val app = new EmberClientTest(short, magicRequestPayloadSize - 1, `1MB`)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Tends to break for longer period, when req = (magic - 1) and res is large") {
      val app = new EmberClientTest(long, magicRequestPayloadSize - 1, `1MB`)
      an[java.io.IOException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }
  }
}
