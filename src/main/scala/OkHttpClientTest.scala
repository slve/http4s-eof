import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import fs2.Stream
import helpers.requestStream
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.client.okhttp.OkHttpBuilder
import org.http4s.implicits._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class OkHttpClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends IOApp {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x" * responsePayloadSize

  override def run(args: List[String]): IO[ExitCode] = {
    def request(client: Client[IO]): Stream[IO, String] = client.stream(req).flatMap(_.bodyText)

    val simpleClient: Resource[IO, OkHttpBuilder[IO]] = {
      val blocker = Blocker.liftExecutionContext(ExecutionContext.global)
      OkHttpBuilder.withDefaultClient[IO](blocker)
    }

    simpleClient.use { client =>
      client.resource.use { c =>
        new BlazeServer(requestStream(request(c), appTime), appTime, response).run(List())
      }
    }
  }

}
