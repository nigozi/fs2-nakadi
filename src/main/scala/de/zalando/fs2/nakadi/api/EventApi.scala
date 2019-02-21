package de.zalando.fs2.nakadi.api

import java.net.URI

import cats.effect.IO
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.http4s.Method.POST

import de.zalando.fs2.nakadi.error.{BatchItemResponse, EventValidation, GeneralError}
import de.zalando.fs2.nakadi.model.{Event, EventTypeName, FlowId, OAuth2TokenProvider}
import io.circe.Encoder

trait EventAlg[F[_]] {
  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(implicit flowId: FlowId): F[Unit]
}

class EventApi(uri: URI, oAuth2TokenProvider: Option[OAuth2TokenProvider]) extends EventAlg[IO] {
  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[EventApi])

  val baseUri: Uri = Uri.unsafeFromString(uri.toString)

  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types" / name.name / "events"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](POST, uri, headers = Headers(headers)).withEntity(events)
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => IO.pure(())
                   case Status.UnprocessableEntity(r) =>
                     r.as[List[BatchItemResponse]].flatMap(e => IO.raiseError(EventValidation(e)))
                   case r => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }
}
