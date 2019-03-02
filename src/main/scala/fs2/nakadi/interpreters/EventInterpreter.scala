package fs2.nakadi.interpreters

import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.nakadi.Implicits
import fs2.nakadi.dsl.HttpClient
import fs2.nakadi.error.{BatchItemResponse, EventValidation, ServerError}
import fs2.nakadi.model._
import io.circe.Encoder
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

class EventInterpreter[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable])
    extends Implicits
    with HttpClient {
  def publish[T](name: EventTypeName,
                 events: List[Event[T]])(implicit config: NakadiConfig[F], flowId: FlowId, enc: Encoder[T]): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name / "events"
    val request    = Request[F](POST, uri).withEntity(events)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      headers <- baseHeaders(config)
      response <- httpClient.fetch[Unit](request.withHeaders(headers)) {
                   case Status.Successful(_) => ().pure[F]
                   case Status.UnprocessableEntity(r) =>
                     r.as[List[BatchItemResponse]].flatMap(e => ME.raiseError(EventValidation(e)))
                   case r => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }
}

object EventInterpreter {
  def apply[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable]): EventInterpreter[F] =
    new EventInterpreter()
}
