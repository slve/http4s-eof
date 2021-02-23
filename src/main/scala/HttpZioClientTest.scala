import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits._
import zio.clock.Clock
import zio.console.{putStrLn, Console}
import zio.duration.Duration
import zio.interop._
import zio.interop.catz._
import zio.stream._
import zio.{App, ExitCode, Task, ZEnv, ZIO, _}

import scala.concurrent.duration.{DurationDouble, FiniteDuration}

class HttpZioClientTest(appTime: FiniteDuration, requestPayloadSize: Int, responsePayloadSize: Int) extends App {

  val uri = uri"http://localhost:8099"
  val body = "x" * requestPayloadSize
  val req = Request[Task](POST, uri).withEntity(body)
  val response = "x" * responsePayloadSize

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val interval = Duration.fromScala(0.01.seconds)
    val timeout = Duration.fromScala(appTime)

    // SERVER OK
    val srv = new ZioBlazeServer(appTime, response)
    val rr: URIO[zio.ZEnv, ExitCode] = srv.run(List())

    def clientManaged = {
      val zioManaged: ZIO[Any, Throwable, ZManaged[Any, Throwable, Client[Task]]] = ZIO.runtime[Any].map { rts =>
        implicit def rr = rts

        // BLAZE WORKS FINE
        val exec = rts.platform.executor.asEC
        import org.http4s.client.blaze._
        catz.catsIOResourceSyntax(BlazeClientBuilder[Task](exec).resource).toManaged

      //// TESTING EMBER
      //import zio.interop.catz.implicits._
      //import org.http4s.ember.client.EmberClientBuilder
      //catz.catsIOResourceSyntax(EmberClientBuilder.default[Task].build).toManaged
      }
      // for our test we need a ZManaged, but right now we've got a ZIO of a ZManaged. To deal with
      // that we create a Managed of the ZIO and then flatten it
      val zm = zioManaged.toManaged_ // toManaged_ provides an empty release of the rescoure
      zm.flatten
    }

    import HClient._
    def clientLive: ZLayer[Any, Throwable, HClient] = ZLayer.fromManaged(clientManaged.map(x => SimpleClient(x)))

    // CLIENT OK
    var i = 0
    val s: ZIO[Console with HClient with Clock, Serializable, Unit] =
      Stream
        .tick(interval)
        .interruptAfter(timeout)
        .foreach(_ => {
          i = i + 1
          // BLAZE CLIENT .as
          val vv: ZIO[Console with HClient, Throwable, Unit] = hClient.client.flatMap { c =>
            c.run(req).toManagedZIO.use { res =>
              val y: ZIO[Console, Throwable, Unit] = res.as[String].flatMap { z =>
                putStrLn(s"$i. ${z.size.toString}")
              }
              //val y: ZIO[Console, Throwable, Unit] =
              //  putStrLn(s"$i. ${res.status.code.toString}")
              y
            }
          }
          vv

          // BLAZE CLIENT STREAMED BODY - fs2 though :(
          //val vv: ZIO[Console with HClient, Serializable, Unit] = hClient.client.flatMap { c =>
          //  c.run(req).toManagedZIO.use { res =>
          //    {
          //      val y: ZIO[Console, Option[Throwable], Unit] = res.bodyText.compile.toList.head.flatMap { z =>
          //        putStrLn(s"$i. ${z.size.toString}")
          //      }
          //      y
          //    }
          //  }
          //}
          //vv

          //  // BLAZE CLIENT STATUS CODE WORKS FINE
          //val vv: ZIO[Console with HClient, Throwable, Unit] = hClient.client.flatMap { c =>
          //  //c.run(req).use{res => res.body.toString.size.toString}
          //  val x: ZIO[Console, Throwable, Unit] =c.status(req).flatMap(x => {
          //    putStrLn(s"$i. ${x.code.toString}")
          //  })
          //  x
          //}
          //vv

          //// TESTING EMBER CLIENT
          //val vv: ZIO[Console with HClient, Nothing, Any] = hClient.client.flatMap { c =>
          //  val x: Task[String] = c.fetchAs[String](req)
          //  x.flatMap(x => putStrLn(x)).merge
          //}
          //vv

          // EMBER CLIENT RUNTIME ERROR: fiber failed NoSuchMethodError
          //val vv: ZIO[Any with Console with HClient, Nothing, ExitCode] = hClient.client.flatMap { c =>
          //  val x: Task[String] = c.fetchAs[String](req)
          //  x.map(x => putStrLn(x)).exitCode
          //}
          //vv

          //vv.merge
          //send(
          //  basicRequest
          //    .streamBody(ZioStreams)(bstream)
          //    .post(uri)
          //).flatMap { response =>
          //  putStrLn(s"$i. ${response.body.map(_.size)}")
          //}
          //.flatMap { response =>
          //  putStrLn(s"$i. ${response.body.map(_.size)}")
          //}

          //send(
          //  basicRequest
          //    .body(body)
          //    .post(uri)
          //    .response(asStreamAlways(ZioStreams)(_.transduce(Transducer.utf8Decode).fold("")(_ + _)))
          //).flatMap { response =>
          //  putStrLn(s"RECEIVED: ${response.body.size}")
          //}
        })
    val z: URIO[ZEnv, ExitCode] = s.provideCustomLayer(clientLive ++ Console.live ++ Clock.live).exitCode
    rr &> z
  }

}
