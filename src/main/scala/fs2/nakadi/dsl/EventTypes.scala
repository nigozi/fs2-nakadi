package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.nakadi.instances.ContextShifts
import fs2.nakadi.interpreters.EventTypeInterpreter
import fs2.nakadi.model._
import fs2.nakadi.{httpClient, randomFlowId}

@finalAlg
trait EventTypes[F[_]] {
  def list(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId()): F[List[EventType]]

  def create(eventType: EventType)(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId()): F[Unit]

  def get(name: EventTypeName)(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId()): F[Option[EventType]]

  def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig[F],
                                                        flowId: FlowId = randomFlowId()): F[Unit]

  def delete(name: EventTypeName)(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId()): F[Unit]
}

object EventTypes extends ContextShifts {
  implicit object ioInterpreter extends EventTypeInterpreter[IO](httpClient[IO])
}
