package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.Pipe
import fs2.nakadi.httpClient
import fs2.nakadi.instances.ContextShifts
import fs2.nakadi.interpreters.EventInterpreter
import fs2.nakadi.model._
import io.circe.Encoder

@finalAlg
trait Events[F[_]] {
  def publish[T](name: EventTypeName, events: List[Event[T]])(implicit config: NakadiConfig[F],
                                                              enc: Encoder[T]): F[Unit]

  def publishStream[T](name: EventTypeName)(implicit config: NakadiConfig[F], enc: Encoder[T]): Pipe[F, Event[T], Unit]
}

object Events extends ContextShifts {
  implicit object ioInterpreter extends EventInterpreter[IO](httpClient[IO])
}
