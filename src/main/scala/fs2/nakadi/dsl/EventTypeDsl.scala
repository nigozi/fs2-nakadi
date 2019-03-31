package fs2.nakadi.dsl
import fs2.nakadi.model._
import fs2.nakadi.randomFlowId

trait EventTypeDsl[F[_]] {
  def list(implicit flowId: FlowId = randomFlowId()): F[List[EventType]]

  def create(eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def get(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Option[EventType]]

  def update(name: EventTypeName, eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def delete(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Unit]
}
