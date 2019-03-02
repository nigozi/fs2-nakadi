package fs2.nakadi.interpreters
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Monad, MonadError}
import fs2.nakadi.Implicits
import fs2.nakadi.dsl.HttpClient
import fs2.nakadi.error.ServerError
import fs2.nakadi.model._
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

class EventTypeInterpreter[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable], M: Monad[F])
    extends Implicits
    with HttpClient {

  def getAll(implicit config: NakadiConfig[F], flowId: FlowId): F[List[EventType]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types"
    val request    = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      headers <- baseHeaders(config)
      response <- httpClient.fetch[List[EventType]](request.withHeaders(headers)) {
                   case Status.Successful(l) => l.as[List[EventType]]
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }

  def create(eventType: EventType)(implicit config: NakadiConfig[F], flowId: FlowId): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types"
    val request    = Request[F](POST, uri).withEntity(eventType)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      headers <- baseHeaders(config)
      response <- httpClient.fetch[Unit](request.withHeaders(headers)) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }

  def get(name: EventTypeName)(implicit config: NakadiConfig[F], flowId: FlowId): F[Option[EventType]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val request    = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      headers <- baseHeaders(config)
      response <- httpClient.fetch[Option[EventType]](request.withHeaders(headers)) {
                   case Status.NotFound(_)   => M.pure(None)
                   case Status.Successful(l) => l.as[EventType].map(_.some)
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }

  def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig[F], flowId: FlowId): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val request    = Request[F](PUT, uri).withEntity(eventType)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      headers <- baseHeaders(config)
      response <- httpClient.fetch[Unit](request.withHeaders(headers)) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }

  def delete(name: EventTypeName)(implicit config: NakadiConfig[F], flowId: FlowId): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val request    = Request[F](DELETE, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      headers <- baseHeaders(config)
      response <- httpClient.fetch[Unit](request.withHeaders(headers)) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }
}

object EventTypeInterpreter {
  def apply[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable], M: Monad[F]): EventTypeInterpreter[F] =
    new EventTypeInterpreter[F]()
}