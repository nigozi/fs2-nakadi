package de.zalando.fs2.nakadi.api
import cats.data.Kleisli
import cats.effect.IO
import cats.tagless._
import de.zalando.fs2.nakadi.impl.EventTypes
import de.zalando.fs2.nakadi.model._

@finalAlg
trait EventTypeApi[F[_]] {
  def list()(implicit flowId: FlowId = randomFlowId()): Kleisli[F, NakadiConfig[F], Either[String, List[EventType]]]

  def create(eventType: EventType)(
      implicit flowId: FlowId = randomFlowId()): Kleisli[F, NakadiConfig[F], Either[String, Unit]]

  def get(name: EventTypeName)(
      implicit flowId: FlowId = randomFlowId()): Kleisli[F, NakadiConfig[F], Either[String, Option[EventType]]]

  def update(name: EventTypeName, eventType: EventType)(
      implicit flowId: FlowId = randomFlowId()): Kleisli[F, NakadiConfig[F], Either[String, Unit]]

  def delete(name: EventTypeName)(
      implicit flowId: FlowId = randomFlowId()): Kleisli[F, NakadiConfig[F], Either[String, Unit]]
}

object EventTypeApi {
  implicit object ioEventTypes extends EventTypeApi[IO] {
    val impl = new EventTypes[IO](httpClient)

    override def list()(implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, List[EventType]]] =
      impl.list()
    override def create(eventType: EventType)(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, Unit]] =
      impl.create(eventType)
    override def get(name: EventTypeName)(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, Option[EventType]]] =
      impl.get(name)
    override def update(name: EventTypeName, eventType: EventType)(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, Unit]] =
      impl.update(name, eventType)
    override def delete(name: EventTypeName)(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, Unit]] =
      impl.delete(name)
  }
}
