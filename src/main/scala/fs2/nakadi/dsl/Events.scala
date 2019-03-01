package fs2.nakadi.dsl
import cats.effect.IO
import cats.free.Free
import cats.free.Free.liftF
import cats.~>
import fs2.nakadi.Implicits
import fs2.nakadi.Util._
import fs2.nakadi.error.{BatchItemResponse, EventValidation, ServerError}
import fs2.nakadi.model._
import io.circe.Encoder
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

sealed trait EventA[T]
case class Publish[A](name: EventTypeName, events: List[Event[A]], config: NakadiConfig, flowId: FlowId)
    extends EventA[Unit]

class Events[T: Encoder] extends Implicits with HttpClient {
  type EventF[A] = Free[EventA, A]

  def publish(name: EventTypeName, events: List[Event[T]])(implicit config: NakadiConfig,
                                                           flowId: FlowId = randomFlowId()): EventF[Unit] =
    liftF[EventA, Unit](Publish(name, events, config, flowId))

  def compiler: EventA ~> IO =
    new (EventA ~> IO) {
      override def apply[A](fa: EventA[A]): IO[A] =
        fa match {
          case p: Publish[T] =>
            implicit val fid: FlowId = p.flowId
            val uri                  = Uri.unsafeFromString(p.config.uri.toString) / "event-types" / p.name.name / "events"
            val request              = Request[IO](POST, uri).withEntity(p.events)

            fetch[Unit](request, p.config) {
              case Status.UnprocessableEntity(r) =>
                r.as[List[BatchItemResponse]].flatMap(e => IO.raiseError(EventValidation(e)))
              case r => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
            }
        }
    }
}

object Events {
  def apply[T: Encoder]: Events[T] = new Events[T]()
}
