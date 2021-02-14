import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import helpers.requestStream
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.duration._

class JdkHttpClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends IOApp {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x" * responsePayloadSize

  override def run(args: List[String]): IO[ExitCode] = {
    def request(client: Client[IO]): Stream[IO, String] = client.stream(req).flatMap(_.bodyText)

    val simpleClient: IO[Client[IO]] = JdkHttpClient.simple[IO]

    simpleClient.flatMap { client =>
      new Server(requestStream(request(client), appTime), appTime, response).run(List())
    }
  }

}
