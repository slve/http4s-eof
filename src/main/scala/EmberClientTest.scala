import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.Stream
import helpers.requestStream
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._

import scala.concurrent.duration._


  // Numbers below vary on different computers
  // In my case, if the request payload size is 65337 or greater
  //  then I get an java.io.IOException on the first request
  // If however the request payload size is 65336 or lower
  //  java.io.IOException doesn't occur, even if the response size is 200MB
  //  while running the test for an extended period

  // broken on first request
  val appTime = 5.seconds
  val requestPayloadSize = 65337
  val responsePayloadSize = 1

  // stable for extended period
  //val appTime = 120.seconds
  //val requestPayloadSize = 65336
  //val responsePayloadSize = 200 * 1000 * 1000
class EmberClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends IOApp {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x" * responsePayloadSize

  var i = 0
  override def run(args: List[String]): IO[ExitCode] = {
    def request(client: Client[IO]): Stream[IO, String] = client.stream(req).flatMap(_.bodyText)

    val simpleClient: Resource[IO, Client[IO]] =
      EmberClientBuilder.default[IO].build

    simpleClient.use { client =>
      new Server(requestStream(request(client), appTime), appTime, response).run(List())
    }
  }

}
