package fs2.nakadi.dsl
import cats.effect.IO
import cats.free.Free
import cats.free.Free.liftF
import cats.~>
import fs2.nakadi.Implicits
import fs2.nakadi.Util._
import fs2.nakadi.error.ServerError
import fs2.nakadi.model._
import org.http4s.Method.{DELETE, PUT}
import org.http4s.dsl.io._
import org.http4s.{Request, Uri}

sealed trait EventTypeA[T]
case class GetAll(config: NakadiConfig, flowId: FlowId)                       extends EventTypeA[List[EventType]]
case class Create(eventType: EventType, config: NakadiConfig, flowId: FlowId) extends EventTypeA[Unit]
case class Get(name: EventTypeName, config: NakadiConfig, flowId: FlowId)     extends EventTypeA[Option[EventType]]
case class Update(name: EventTypeName, eventType: EventType, config: NakadiConfig, flowId: FlowId)
    extends EventTypeA[Unit]
case class Delete(name: EventTypeName, config: NakadiConfig, flowId: FlowId) extends EventTypeA[Unit]

object EventTypes extends Implicits with HttpClient {
  type EventTypeF[A] = Free[EventTypeA, A]

  def getAll(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): EventTypeF[List[EventType]] =
    liftF[EventTypeA, List[EventType]](GetAll(config, flowId))

  def create(eventType: EventType)(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): EventTypeF[Unit] =
    liftF[EventTypeA, Unit](Create(eventType, config, flowId))

  def get(name: EventTypeName)(implicit config: NakadiConfig,
                               flowId: FlowId = randomFlowId()): EventTypeF[Option[EventType]] =
    liftF[EventTypeA, Option[EventType]](Get(name, config, flowId))

  def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig,
                                                        flowId: FlowId = randomFlowId()): EventTypeF[Unit] =
    liftF[EventTypeA, Unit](Update(name, eventType, config, flowId))

  def delete(name: EventTypeName)(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): EventTypeF[Unit] =
    liftF[EventTypeA, Unit](Delete(name, config, flowId))

  def compiler: EventTypeA ~> IO =
    new (EventTypeA ~> IO) {
      override def apply[A](fa: EventTypeA[A]): IO[A] =
        fa match {
          case GetAll(config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "event-types"
            val request              = Request[IO](GET, uri)

            fetch[List[EventType]](request, config)(r =>
              r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
          case Create(eventType, config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "event-types"
            val request              = Request[IO](POST, uri).withEntity(eventType)

            fetch[Unit](request, config)(r => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
          case Get(name, config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
            val request              = Request[IO](GET, uri)

            fetchOpt[EventType](request, config)(r =>
              r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
          case Update(name, eventType, config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
            val request              = Request[IO](PUT, uri).withEntity(eventType)

            fetch[Unit](request, config)(r => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
          case Delete(name, config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
            val request              = Request[IO](DELETE, uri)

            fetch[Unit](request, config)(r => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
        }
    }
}
