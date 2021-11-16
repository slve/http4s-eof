import cats.effect.{Concurrent, IO, Timer}
import fs2.Stream

import scala.concurrent.duration._

package object helpers {

  def requestStream(
    request: Stream[IO, String],
    appTime: FiniteDuration
  )(implicit c: Concurrent[IO], t: Timer[IO]): Stream[IO, Unit] = {

    var i = 0
    Stream
      .fixedRate(0.01.second)
      .flatMap(_ => {
        i = i + 1

        request
      })
      //.evalMap(c => IO.delay(println(s"$i ${c.size}")))
      .evalMap(_ => IO.delay(()))
      .interruptAfter(appTime)
  }

}
