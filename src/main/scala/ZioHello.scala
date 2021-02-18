import zio._
import zio.console._

object ZioHello extends App {
  override def run(args: List[String]) = {
    val myAppLogic =
      for {
        _ <- putStrLn("Hello! What is your name?")
        name <- getStrLn
        _ <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
      } yield ()

    val z: URIO[Console, ExitCode] = myAppLogic.exitCode
    z
  }
}
