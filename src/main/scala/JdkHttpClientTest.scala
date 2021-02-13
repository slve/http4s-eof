package io.bitrise.apm.symbolicator

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, _}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object JdkHttpClientTest extends IOApp {

  val appTime = 300.seconds
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

  val uri = uri"http://localhost:8099"
  val body = "x".repeat(requestPayloadSize)
  val req = Request[IO](POST, uri).withEntity(body)
  val response = "x".repeat(responsePayloadSize)

  var i = 0
  override def run(args: List[String]): IO[ExitCode] = {
    simpleClient.flatMap(client => {
      val requestStream: Stream[IO, Unit] = Stream
        .fixedRate(0.01.second)
        .flatMap(_ => {
          i = i + 1
          val y: Stream[IO, Response[IO]] = client.stream(req)
          y.flatMap(r => r.bodyText)
        })
        .evalMap(c => IO.delay(println(s"$i ${c.size}")))
        .interruptAfter(appTime)

      server(requestStream)
    })
  }

  import java.net.http.HttpClient
  val client0: IO[Client[IO]] = IO {
    HttpClient
      .newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build()

  }.map(JdkHttpClient(_))

  val simpleClient: IO[Client[IO]] = JdkHttpClient.simple[IO]

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
