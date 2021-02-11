package io.bitrise.apm.symbolicator

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import org.http4s.{HttpRoutes, _}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Http4sEof extends IOApp {

  val appTime = 30.seconds
  // If request payload size is 32603 or greater
  //  AND response payload size is 81161 or greater
  //  then we get EOF exception in some but not all cases
  // If however either of these payload sizes is lower then
  //  EOF exception doesn't occur, even if running for an extended period

  // unstable
  val requestPayloadSize = 32603
  val responsePayloadSize = 81161

  // stable
  // val requestPayloadSize = 32602
  // val responsePayloadSize = 81161

  // stable
  // val requestPayloadSize = 32603
  // val responsePayloadSize = 81160

  val uri = uri"http://localhost:8099"
  val body = "x".repeat(requestPayloadSize)
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x".repeat(responsePayloadSize)

  var i = 0
  override def run(args: List[String]): IO[ExitCode] = {
    val requestStream: Stream[IO, Unit] = Stream
      .fixedRate(0.01.second)
      .flatMap(_ => {
        i = i + 1

        simpleClient.stream
          .flatMap(c => c.stream(req))
          .flatMap(_.bodyText)
      })
      .evalMap(c => IO.delay(println(s"$i ${c.size}")))
      .interruptAfter(appTime)

    server(requestStream)
  }

  val simpleClient: BlazeClientBuilder[IO] =
    BlazeClientBuilder[IO](ExecutionContext.global)
      .withRequestTimeout(45.seconds)
      .withIdleTimeout(1.minute)
      .withResponseHeaderTimeout(44.seconds)

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
