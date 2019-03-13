package fs2.nakadi.dsl

import cats.effect.{ContextShift, IO}

import scala.concurrent.ExecutionContext.Implicits.global

trait Implicits {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
}

object Implicits extends Implicits
