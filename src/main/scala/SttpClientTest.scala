import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import helpers.requestStream
import sttp.client3._

import scala.concurrent.duration._

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
