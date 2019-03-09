package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.nakadi.dsl.Implicits._
import fs2.nakadi.interpreters.EventInterpreter
import fs2.nakadi.model._
import io.circe.Encoder

@finalAlg
trait Events[F[_]] {
  def publish[T](name: EventTypeName, events: List[Event[T]])(implicit config: NakadiConfig[F],
                                                              enc: Encoder[T]): F[Unit]
}

object Events {
  implicit object ioInterpreter extends EventInterpreter[IO]
}
