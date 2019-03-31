package fs2.nakadi.client

import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import fs2.nakadi.dsl.EventDsl
import fs2.nakadi.error.{BatchItemResponse, EventValidation}
import fs2.nakadi.httpClient
import fs2.nakadi.implicits._
import fs2.nakadi.model._
import fs2.{Pipe, Stream}
import io.circe.Encoder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.{Request, Status, Uri}

class EventClient[F[_]: Async](httpClient: Client[F])(implicit config: NakadiConfig[F], M: MonadError[F, Throwable])
    extends EventDsl[F] {
  private val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[EventClient[F]])

  override def publish[T](name: EventTypeName, events: List[Event[T]])(implicit flowId: FlowId,
                                                                       enc: Encoder[T]): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "event-types" / name.name / "events"
    val req = Request[F](POST, uri).withEntity(encode(events))

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case Status.UnprocessableEntity(r) =>
                     r.as[List[BatchItemResponse]].flatMap(e => M.raiseError(EventValidation(e)))
                   case r => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def publishStream[T](name: EventTypeName)(implicit flowId: FlowId,
                                                     enc: Encoder[T]): Pipe[F, Event[T], Unit] =
    _.chunks.evalMap(chunk => publish(name, chunk.toList)).handleErrorWith(Stream.raiseError[F])
}

object EventClient {
  def apply[F[_]: Async: ContextShift](implicit config: NakadiConfig[F]): EventDsl[F] = new EventClient(httpClient[F])
}
