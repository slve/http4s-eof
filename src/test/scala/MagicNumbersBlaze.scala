import constants._
import constants.blaze._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers

class MagicNumbersBlaze extends AnyFeatureSpec with Matchers {

  Feature("Blaze client") {
    Scenario("Is always broken at first request, when req = magic AND res = magic") {
      val app = new BlazeClientTest(short, magicRequestPayloadSize, magicResponsePayloadSize)
      an[org.http4s.InvalidBodyException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Tends to hold for a short period, when req = (magic - 1) AND res = magic") {
      val app = new BlazeClientTest(short, magicRequestPayloadSize - 1, magicResponsePayloadSize)
      app.run(List()).unsafeRunSync().code mustBe 0
    }

    Scenario("Tends to break on a longer run, when req = (magic - 1) AND res = magic") {
      val app = new BlazeClientTest(long, magicRequestPayloadSize - 1, magicResponsePayloadSize)
      an[org.http4s.InvalidBodyException] should be thrownBy
        app.run(List()).unsafeRunSync()
    }

    Scenario("Tends to hold on a longer run, when req = magic AND res = (magic - 1)") {
      val app = new BlazeClientTest(long, magicRequestPayloadSize, magicResponsePayloadSize - 1)
      app.run(List()).unsafeRunSync().code mustBe 0
    }
  }
}
