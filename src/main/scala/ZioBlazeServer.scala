import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.duration.Duration
import zio.console.putStrLn
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.duration._

//app: Stream[IO, Unit]
class ZioBlazeServer(appTime: FiniteDuration, response: String) extends App {

  private val dsl = Http4sDsl[Task]
  import dsl._

  def run(args: List[String]): zio.URIO[zio.ZEnv, ExitCode] =
    ZIO
      .runtime[ZEnv]
      .flatMap { implicit runtime =>
        BlazeServerBuilder[Task](runtime.platform.executor.asEC)
          .withIdleTimeout(5.minutes)
          .bindHttp(8099, "0.0.0.0")
          .withHttpApp(
            HttpRoutes
              .of[Task] {
                case POST -> Root => Ok(response)
              }
              .orNotFound
          )
          .resource
          .toManagedZIO
          .useForever
          .timeout(Duration.fromScala(appTime + 2.seconds))
          //.once
          //.run
          //.exitCode
          .as(ExitCode.success)
          .foldCauseM(err => putStrLn(err.prettyPrint).as(ExitCode.failure), _ => ZIO.succeed(ExitCode.success))
      }

}

object ZioBlazeServer {

  private val dsl = Http4sDsl[Task]
  import dsl._

  import org.http4s.server.Server
  type Http4Server = Has[Server[Task]]

  def createHttp4sLayer(response: String): ZLayer[ZEnv, Throwable, Http4Server] =
    ZLayer.fromManaged(createHttp4Server(response))

  def createHttp4Server(response: String): ZManaged[ZEnv, Throwable, Server[Task]] =
    ZManaged.runtime[ZEnv].flatMap { implicit runtime: Runtime[ZEnv] =>
      BlazeServerBuilder[Task](runtime.platform.executor.asEC)
        .bindHttp(8099, "0,0,0,0")
        .withHttpApp(
          HttpRoutes
            .of[Task] {
              case POST -> Root => Ok(response)
            }
            .orNotFound
        )
        .resource
        .toManagedZIO
    }
}
