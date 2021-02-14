import scala.concurrent.duration._

package object constants {
  val `1kB` = 1000
  val `1MB` = 1000 * 1000
  val `20MB` = 20 * 1000 * 1000
  val long = 30.seconds
  val short = 2.seconds

  // Numbers below may vary on different computers
  object blaze {
    val magicRequestPayloadSize = 65398
    val magicResponsePayloadSize = 81161
  }

  object ember {
    val magicRequestPayloadSize = 65337
  }

  object jdkhttp {
    val magicRequestPayloadSize = 8 * 65416 + 1 // 523329
    val magicResponsePayloadSize = 8 * 8177 + 1 // 65417
  }

  object sttp {
    val magicRequestPayloadSize = 65289
    val magicResponsePayloadSize = 81161
  }
}
