import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import helpers.requestStream
import sttp.client3._

import scala.concurrent.duration._


  // Numbers below vary on different computers
  // In my case, if the request payload size is 65289 or greater
  //  AND response payload size is 81161 or greater
  //  then I get a sttp.client3.SttpClientException$ReadException on the first request
  // If however either of these payload sizes is lower then
  //  ReadException doesn't occur, even if running for an extended period

  // broken at first request
  val appTime = 5.seconds
  val requestPayloadSize = 65289
  val responsePayloadSize = 81161

  // can hold for 5 seconds, but broken on longer run, like 30 seconds appTime
  //val appTime = 30.seconds
  //val requestPayloadSize = 65288
  //val responsePayloadSize = 81161

  // more stable, can hold up to 30 seconds appTime, seen broken on 120 seconds
  //val appTime = 120.seconds
  //val requestPayloadSize = 65289
  //val responsePayloadSize = 81160
class SttpClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends IOApp {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val response = "x" * responsePayloadSize
  val backend = HttpURLConnectionBackend()

  override def run(args: List[String]): IO[ExitCode] = {

    def request: Stream[IO, String] = Stream.eval {
      IO(
        basicRequest
          .body(body)
          .post(uri)
          .send(backend)
          .body
          .toOption
          .get // let's just throw if fails to get the response body
      )
    }

    new Server(requestStream(request, appTime), appTime, response).run(List())
  }

}
