import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import fs2.Stream
import helpers.requestStream
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


  // Numbers below vary on different computers
  // In my case, if the request payload size is 65398 or greater
  //  AND response payload size is 81161 or greater
  //  then I get an EOF exception in some but not all cases
  // If however either of these payload sizes is lower then
  //  EOF exception doesn't occur, even if running for an extended period

  // broken on first request
  val appTime = 5.seconds
  val requestPayloadSize = 65398
  val responsePayloadSize = 81161

  // can hold for 5 seconds, but broken on longer run, like 30 seconds appTime
  //val appTime = 30.seconds
  //val requestPayloadSize = 65397
  //val responsePayloadSize = 81161

  // more stable, can hold up to 30 seconds appTime, seen broken on 120 seconds
  //val appTime = 30.seconds
  //val requestPayloadSize = 65398
  //val responsePayloadSize = 81160
class BlazeClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends IOApp {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x" * responsePayloadSize

  override def run(args: List[String]): IO[ExitCode] = {
    def request(client: Client[IO]): Stream[IO, String] = client.stream(req).flatMap(_.bodyText)

    def simpleClient(implicit c: ConcurrentEffect[IO]): BlazeClientBuilder[IO] =
      BlazeClientBuilder[IO](ExecutionContext.global)
        .withRequestTimeout(45.seconds)
        .withIdleTimeout(1.minute)
        .withResponseHeaderTimeout(44.seconds)

    new Server(simpleClient.stream.flatMap(c => requestStream(request(c), appTime)), appTime, response).run(List())
  }

}
