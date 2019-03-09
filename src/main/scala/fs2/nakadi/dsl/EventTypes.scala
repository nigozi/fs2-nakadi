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

object EventTypes {
  implicit object ioInterpreter extends EventTypes[IO] with Implicits {
    override def list(implicit config: NakadiConfig[IO]): IO[List[EventType]] =
      EventTypeInterpreter[IO].list

    override def create(eventType: EventType)(implicit config: NakadiConfig[IO]): IO[Unit] =
      EventTypeInterpreter[IO].create(eventType)

    override def get(name: EventTypeName)(implicit config: NakadiConfig[IO]): IO[Option[EventType]] =
      EventTypeInterpreter[IO].get(name)

    override def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig[IO]): IO[Unit] =
      EventTypeInterpreter[IO].update(name, eventType)

    override def delete(name: EventTypeName)(implicit config: NakadiConfig[IO]): IO[Unit] =
      EventTypeInterpreter[IO].delete(name)
  }
}
