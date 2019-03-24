package fs2.nakadi.interpreters
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Monad, MonadError}
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import fs2.nakadi.dsl.EventTypes
import fs2.nakadi.implicits._
import fs2.nakadi.model._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

class EventTypeInterpreter[F[_]: Async: ContextShift](httpClient: Client[F])(implicit ME: MonadError[F, Throwable],
                                                                             M: Monad[F])
    extends EventTypes[F]
    with Interpreter {
  private val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[EventTypeInterpreter[F]])

  override def list(implicit config: NakadiConfig[F], flowId: FlowId): F[List[EventType]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[List[EventType]](request) {
                   case Status.Successful(l) => l.as[List[EventType]]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def create(eventType: EventType)(implicit config: NakadiConfig[F], flowId: FlowId): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types"
    val req = Request[F](POST, uri).withEntity(encode(eventType))

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def get(name: EventTypeName)(implicit config: NakadiConfig[F], flowId: FlowId): F[Option[EventType]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[Option[EventType]](request) {
                   case Status.NotFound(_)   => M.pure(None)
                   case Status.Successful(l) => l.as[EventType].map(_.some)
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig[F], flowId: FlowId): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val req = Request[F](PUT, uri).withEntity(encode(eventType))

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def delete(name: EventTypeName)(implicit config: NakadiConfig[F], flowId: FlowId): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val req = Request[F](DELETE, uri)

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }
}
