package io.bitrise.apm.symbolicator

import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.Stream
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object EmberClientTest extends IOApp {

  // Numbers below vary on different computers
  // In my case, if the request payload size is 65337 or greater
  //  then I get an EOF exception on the first request
  // If however the request payload size is 65336 or lower
  //  EOF exception doesn't occur, even if the response size is 200MB
  //  while running the test for an extended period

  // broken on first request
  val appTime = 5.seconds
  val requestPayloadSize = 65337
  val responsePayloadSize = 1

  // stable for extended period
  //val appTime = 120.seconds
  //val requestPayloadSize = 65336
  //val responsePayloadSize = 200 * 1000 * 1000

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x" * responsePayloadSize

  var i = 0
  override def run(args: List[String]): IO[ExitCode] = {
    def requestStream(client: Client[IO]): Stream[IO, Unit] =
      Stream
        .fixedRate(0.01.second)
        .flatMap(_ => {
          i = i + 1

          client.stream(req).flatMap(_.bodyText)
        })
        .evalMap(c => IO.delay(println(s"$i ${c.size}")))
        .interruptAfter(appTime)

    simpleClient.use { client =>
      server(requestStream(client))
    }
  }

  val simpleClient: Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build

  def server(app: Stream[IO, Unit]) =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .withIdleTimeout(5.minutes)
      .bindHttp(8099, "0.0.0.0")
      .withHttpApp(
        HttpRoutes
          .of[IO] {
            case POST -> Root => Ok(response)
          }
          .orNotFound
      )
      .serve
      .concurrently(app)
      .interruptAfter(appTime + 2.seconds)
      .compile
      .drain
      .as(ExitCode.Success)

}
