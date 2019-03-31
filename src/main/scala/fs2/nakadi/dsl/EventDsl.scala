package fs2.nakadi.dsl
import fs2.Pipe
import fs2.nakadi.model._
import fs2.nakadi.randomFlowId
import io.circe.Encoder

trait EventDsl[F[_]] {
  def publish[T](name: EventTypeName, events: List[Event[T]])(implicit flowId: FlowId = randomFlowId(),
                                                              enc: Encoder[T]): F[Unit]

  def publishStream[T](name: EventTypeName)(implicit flowId: FlowId = randomFlowId(),
                                            enc: Encoder[T]): Pipe[F, Event[T], Unit]
}
