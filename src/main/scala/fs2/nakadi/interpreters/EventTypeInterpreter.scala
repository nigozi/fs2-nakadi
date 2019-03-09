package fs2.nakadi.interpreters
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Monad, MonadError}
import fs2.nakadi.model._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

class EventTypeInterpreter[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable], M: Monad[F])
    extends HttpClient {

  def list(implicit config: NakadiConfig[F]): F[List[EventType]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types"
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[List[EventType]](request) {
                   case Status.Successful(l) => l.as[List[EventType]]
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def create(eventType: EventType)(implicit config: NakadiConfig[F]): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types"
    val req        = Request[F](POST, uri).withEntity(encode(eventType))
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def get(name: EventTypeName)(implicit config: NakadiConfig[F]): F[Option[EventType]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Option[EventType]](request) {
                   case Status.NotFound(_)   => M.pure(None)
                   case Status.Successful(l) => l.as[EventType].map(_.some)
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def update(name: EventTypeName, eventType: EventType)(implicit config: NakadiConfig[F]): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val req        = Request[F](PUT, uri).withEntity(encode(eventType))
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def delete(name: EventTypeName)(implicit config: NakadiConfig[F]): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name
    val req        = Request[F](DELETE, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => throwServerError(r)
                 }
    } yield response
  }
}

object EventTypeInterpreter {
  def apply[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable], M: Monad[F]): EventTypeInterpreter[F] =
    new EventTypeInterpreter[F]()
}
