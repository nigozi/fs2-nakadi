package fs2.nakadi.instances
import cats.effect.{ContextShift, IO}

import scala.concurrent.ExecutionContext.Implicits.global

trait ContextShifts {
  implicit val ioCs: ContextShift[IO] = IO.contextShift(global)
}
