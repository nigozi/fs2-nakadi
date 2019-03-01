package fs2.nakadi.api
import cats.effect.IO
import fs2.nakadi.Util._
import fs2.nakadi.dsl.EventTypes
import fs2.nakadi.model.{EventType, EventTypeName, FlowId, NakadiConfig}

object EventTypeApi {
  def getAll(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): IO[List[EventType]] =
    EventTypes.getAll.foldMap(EventTypes.compiler)

  def create(eventType: EventType)(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): IO[Unit] =
    EventTypes.create(eventType).foldMap(EventTypes.compiler)

  def get(name: EventTypeName)(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): IO[Option[EventType]] =
    EventTypes.get(name).foldMap(EventTypes.compiler)

  def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig,
                                                        flowId: FlowId = randomFlowId()): IO[Unit] =
    EventTypes.update(name, eventType).foldMap(EventTypes.compiler)

  def delete(name: EventTypeName)(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): IO[Unit] =
    EventTypes.delete(name).foldMap(EventTypes.compiler)
}
