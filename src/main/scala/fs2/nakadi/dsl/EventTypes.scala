package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.nakadi.interpreters.EventTypeInterpreter
import fs2.nakadi.model._

@finalAlg
trait EventTypes[F[_]] {
  def list(implicit config: NakadiConfig[F]): F[List[EventType]]

  def create(eventType: EventType)(implicit config: NakadiConfig[F]): F[Unit]

  def get(name: EventTypeName)(implicit config: NakadiConfig[F]): F[Option[EventType]]

  def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig[F]): F[Unit]

  def delete(name: EventTypeName)(implicit config: NakadiConfig[F]): F[Unit]
}

object EventTypes extends Implicits {
  implicit object ioInterpreter extends EventTypeInterpreter[IO](httpClient[IO])
}
