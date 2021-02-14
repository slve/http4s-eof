import constants.sttp._
import constants.{long, short}
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers

class MagicNumbersSttp extends AnyFeatureSpec with Matchers {

  Feature("Sttp client") {
    Scenario("Is always broken at first request, when req = magic AND res = magic") {
      val app = new SttpClientTest(short, magicRequestPayloadSize, magicResponsePayloadSize)
      an[sttp.client3.SttpClientException.ReadException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Tends to hold for a short period, when req = (magic - 1) AND res = magic") {
      val app = new SttpClientTest(short, magicRequestPayloadSize - 1, magicResponsePayloadSize)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Tends to break on a longer run, when req = (magic - 1) AND res = magic") {
      val app = new SttpClientTest(long, magicRequestPayloadSize - 1, magicResponsePayloadSize)
      an[sttp.client3.SttpClientException.ReadException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Tends to hold on a longer run, when req = magic AND res = (magic - 1)") {
      val app = new SttpClientTest(long, magicRequestPayloadSize, magicResponsePayloadSize - 1)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }
}
