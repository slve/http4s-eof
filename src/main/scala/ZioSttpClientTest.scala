package sttp.client3.examples

import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient, send}
import zio._
import zio.console._
import zio.stream._

object StreamZio extends App {
  def streamRequestBody: RIO[Console with SttpClient, Unit] = {
    val stream = Stream("Hello, world".getBytes.toIndexedSeq: _*)

    send(
      basicRequest
        .streamBody(ZioStreams)(stream)
        .post(uri"https://httpbin.org/post")
    ).flatMap { response => putStrLn(s"RECEIVED:\n${response.body}") }
  }

  def streamResponseBody: RIO[Console with SttpClient, Unit] = send(
    basicRequest
      .body("I want a stream!")
      .post(uri"https://httpbin.org/post")
      .response(asStreamAlways(ZioStreams)(_.transduce(Transducer.utf8Decode).fold("")(_ + _)))
  ).flatMap { response => putStrLn(s"RECEIVED:\n${response.body}") }

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = (streamRequestBody *> streamResponseBody)
    .provideCustomLayer(AsyncHttpClientZioBackend.layer())
    .exitCode
}