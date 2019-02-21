package de.zalando.fs2.nakadi.api
import java.net.URI

import cats.effect.IO
import cats.syntax.option._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.http4s.Method._

import de.zalando.fs2.nakadi.error.GeneralError
import de.zalando.fs2.nakadi.model._

trait EventTypeAlg[F[_]] {
  def list(implicit flowId: FlowId = randomFlowId()): F[List[EventType]]

  def create(eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def get(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Option[EventType]]

  def update(name: EventTypeName, eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def delete(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Unit]
}

class EventTypeApi(uri: URI, oAuth2TokenProvider: Option[OAuth2TokenProvider]) extends EventTypeAlg[IO] {
  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[EventTypeApi])

  val baseUri: Uri = Uri.unsafeFromString(uri.toString)

  override def list(implicit flowId: FlowId): IO[List[EventType]] = {
    val uri         = baseUri / "event-types"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[List[EventType]](request) {
                   case Status.Successful(l) => l.as[List[EventType]]
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }

  override def create(eventType: EventType)(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](POST, uri, headers = Headers(headers)).withEntity(eventType)
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => IO.pure(())
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }

  override def get(name: EventTypeName)(implicit flowId: FlowId): IO[Option[EventType]] = {
    val uri         = baseUri / "event-types" / name.name
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[Option[EventType]](request) {
                   case Status.NotFound(_)   => IO.pure(None)
                   case Status.Successful(l) => l.as[EventType].map(_.some)
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }

  override def update(name: EventTypeName, eventType: EventType)(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types" / name.name
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](PUT, uri, headers = Headers(headers)).withEntity(eventType)
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => IO.pure(())
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }

  override def delete(name: EventTypeName)(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types" / name.name
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](DELETE, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(l) => IO.pure(())
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }
}
