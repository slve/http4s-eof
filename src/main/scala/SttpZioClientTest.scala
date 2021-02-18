import sttp.capabilities.zio.ZioStreams
import sttp.client3._
import zio.clock.Clock
import zio.stream._
import zio.{App, ExitCode, ZEnv, ZIO}
import zio.duration.Duration

import scala.concurrent.duration.{DurationDouble, FiniteDuration}
//import scala.math.Ordered.orderingToOrdered

class SttpZioClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends App {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  //val response = "x" * responsePayloadSize

  //val server = new BlazeServer(
  //  fs2.Stream
  //    .fixedRate(0.01.second)(cats.effect.IO.timer(ExecutionContext.global))
  //    .evalTap(_ => cats.effect.IO.pure(()))
  //    .map(_ => ()),
  //  appTime,
  //  response
  //).run(List())

  def request: RequestT[Identity, Either[String, Stream[Throwable, Byte]], Any with ZioStreams] =
    quickRequest
      .body(body)
      .post(uri)
      .response(asStreamUnsafe(ZioStreams))
      .readTimeout(2.seconds)

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    import zio._
    val interval = Duration.fromScala(0.01.seconds)
    val timeout = Duration.fromScala(appTime)
    //val yyy: Schedule[Any, Any, duration.Duration] =
    //  (Schedule.spaced(interval) >>> Schedule.elapsed).whileOutput(_ < timeout)

    // LAYERED SOLUTION BROKEN AT RUNTIME
    //import ZioBlazeServer.Http4Server
    //import org.http4s.server.Server
    //import zio.console.Console
    //val program: ZIO[Has[Server[Task]] with Console, Nothing, Nothing] =
    //  ZIO.interrupt
    //val httpServerLayer: ZLayer[ZEnv, Throwable, Http4Server] = ZioBlazeServer.createHttp4sLayer(response)
    //val pp: URIO[zio.ZEnv with Any with Console, ExitCode] = program
    //  .provideLayer(httpServerLayer ++ Console.live)
    //  .exitCode
    //pp

    // SERVER OK
    val srv = new ZioBlazeServer(appTime, "x" * responsePayloadSize)
    val rr: URIO[zio.ZEnv, ExitCode] = srv.run(List())

    // CLIENT OK
    import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
    import sttp.client3.httpclient.zio.{SttpClient, send}
    import zio.console.{Console, putStrLn}
    var i = 0
    val s: ZIO[SttpClient with Console with Clock, Throwable, Unit] =
      Stream
        .tick(interval)
        .interruptAfter(timeout)
        .foreach(_ => {
          i = i + 1
          putStrLn(s"Before") *>
            send(request)
              .flatMap { x =>
                putStrLn(x.code.toString)
              }
              .flatMap(_ => putStrLn(s"x ${i}")) *>
            putStrLn(s"After")
          //  putStrLn(request.method.toString)
          //.map(r => putStrLn(r.body.toString.size.toString))
        })
    //val sttpbe: ZLayer[Any, Throwable, SttpClient] = HttpClientZioBackend.layer()
    val sttpbe: ZLayer[Any, Throwable, SttpClient] = AsyncHttpClientZioBackend.layer()
    val z: URIO[ZEnv, ExitCode] = s.provideCustomLayer(sttpbe ++ Console.live ++ Clock.live).exitCode
    rr &> z

    // PRINT OK
    //import zio.console.putStrLn
    //import zio.console.Console
    //var i = 0
    ////val s: ZIO[Console with Clock, Nothing, Unit] = Stream
    ////  .fromSchedule(yyy)
    ////  .interruptAfter(timeout)
    ////  .foreach(_ => {i = i+1 ;putStrLn(s"x ${i}")})
    //val s: ZIO[Console with Clock, Nothing, Unit] = Stream
    //  .tick(interval)
    //  .interruptAfter(timeout)
    //  .foreach(_ => {
    //    i = i + 1
    //    putStrLn(s"x ${i}")
    //  })
    //val z = s.provideLayer(Clock.live ++ Console.live).run.exitCode
    //z

    //val s: ZIO[Console, Nothing, Unit] = Stream.repeat(Schedule.spaced(interval)).foreach(_ => putStrLn("x"))
    //val b: ZIO[SttpClient, Throwable, Unit] = s.flatMap { _ =>
    //  send(request).map(r => r.body.toString.size)
    //}
    //val z: URIO[ZEnv, ExitCode] = b.provideLayer(sstr).forever.run.exitCode //.flatMap(_ => rr)

  }

}
