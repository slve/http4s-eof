import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import helpers.requestStream
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.duration._

  // Numbers below may vary on different computers
  // In my case, if the request payload size is 523329 or greater
  //  OR response payload size is 65417 or greater
  //  then I get a java.io.IOException: fixed content-length: 65416, bytes received: 49032
  //  in some but not all cases.
  // If however both of these payload sizes are lower then
  //  fixed content-length exception doesn't occur, even if running for an extended period.
  //  Yet in some rare cases I get a java.io.IOException: HTTP/1.1 header parser received no bytes

  // stable - at least for content-length
  val requestPayloadSize = 523328 // it's 8 times 65416 !?
  val responsePayloadSize = 65416 // it's 8 times 8177 !?

  // broken - yet fairly stable
  // val requestPayloadSize = 523329
  // val responsePayloadSize = 65416

  // quite broken
  // val requestPayloadSize = 523328
  // val responsePayloadSize = 65417
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
