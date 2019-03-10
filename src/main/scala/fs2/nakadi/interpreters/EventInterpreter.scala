package fs2.nakadi.interpreters

import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.nakadi.dsl.Events
import fs2.nakadi.error.{BatchItemResponse, EventValidation}
import fs2.nakadi.model._
import io.circe.Encoder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

class EventInterpreter[F[_]: Async: ContextShift](httpClient: Client[F])(implicit ME: MonadError[F, Throwable])
    extends Events[F] {

  def publish[T](name: EventTypeName, events: List[Event[T]])(implicit config: NakadiConfig[F],
                                                              enc: Encoder[T]): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name / "events"
    val req = Request[F](POST, uri).withEntity(encode(events))

    for {
      request <- addHeaders(req, config)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => ().pure[F]
                   case Status.UnprocessableEntity(r) =>
                     r.as[List[BatchItemResponse]].flatMap(e => ME.raiseError(EventValidation(e)))
                   case r => throwServerError(r)
                 }
    } yield response
  }
}
