package fs2.nakadi.api
import cats.effect.IO
import fs2.nakadi.Util._
import fs2.nakadi.dsl.Events
import fs2.nakadi.model.{Event, EventTypeName, FlowId, NakadiConfig}
import io.circe.Encoder

object EventApi {
  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(implicit config: NakadiConfig,
                                                                       flowId: FlowId = randomFlowId()): IO[Unit] = {
    val eventF = Events[T]

    eventF.publish(name, events).foldMap(eventF.compiler)
  }
}
