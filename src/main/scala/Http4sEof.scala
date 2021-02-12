package io.bitrise.apm.symbolicator

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Http4sEof extends IOApp {

  val appTime = 30.seconds
  // Numbers below vary on different computers
  // In my case, if the request payload size is 32603 or greater
  //  AND response payload size is 81161 or greater
  //  then I get an EOF exception in some but not all cases
  // If however either of these payload sizes is lower then
  //  EOF exception doesn't occur, even if running for an extended period

  // broken at first request
  val requestPayloadSize = 81161
  val responsePayloadSize = 81161

  // fairly stable
  // val requestPayloadSize = 81160
  // val responsePayloadSize = 81160

  // stable
  // val requestPayloadSize = 81100
  // val responsePayloadSize = 81100

  val body = "x".repeat(requestPayloadSize)
  val response = "x".repeat(responsePayloadSize)

  var i = 0
  override def run(args: List[String]): IO[ExitCode] = {
    import sttp.client3._

    val uri = uri"http://localhost:8099"

    import sttp.client3._

    val backend = HttpURLConnectionBackend()

    val requestStream: Stream[IO, Unit] = Stream
      .fixedRate(0.01.second)
      .map(_ => {
        i = i + 1

        basicRequest
          .body(body)
          .post(uri)
          .send(backend).body.toOption.get // let's just throw if fails to get
      })
      .evalMap(c => IO.delay(println(s"$i ${c.size}")))
      .interruptAfter(appTime)

    server(requestStream)
  }

  def server(app: Stream[IO, Unit]) = {
    import org.http4s.HttpRoutes
    import org.http4s.dsl.io._
    import org.http4s.implicits._
    import org.http4s.server.blaze.BlazeServerBuilder

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

}
