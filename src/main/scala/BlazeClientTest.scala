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

    new BlazeServer(simpleClient.stream.flatMap(c => requestStream(request(c), appTime)), appTime, response).run(List())
  }

}
