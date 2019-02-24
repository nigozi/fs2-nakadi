package fs2.nakadi.api

import cats.effect.IO
import cats.syntax.applicative._
import cats.syntax.option._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.http4s.Method._

import fs2.nakadi.error._
import fs2.nakadi.model._

trait EventTypeAlg[F[_]] {
  def list(implicit flowId: FlowId = randomFlowId()): F[List[EventType]]

  def create(eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def get(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Option[EventType]]

  def update(name: EventTypeName, eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def delete(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Unit]
}

class EventTypes(config: NakadiConfig) extends EventTypeAlg[IO] with Implicits {
  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[EventTypes])

  private val baseUri       = Uri.unsafeFromString(config.uri.toString)
  private val tokenProvider = config.tokenProvider
  private val httpClient    = config.httpClient.getOrElse(defaultClient)

  override def list(implicit flowId: FlowId): IO[List[EventType]] = {
    val uri         = baseUri / "event-types"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[List[EventType]](request) {
                     case Status.Successful(l) => l.as[List[EventType]]
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }

  override def create(eventType: EventType)(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](POST, uri, headers = Headers(headers)).withEntity(eventType)
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[Unit](request) {
                     case Status.Successful(_) => ().pure[IO]
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }

  override def get(name: EventTypeName)(implicit flowId: FlowId): IO[Option[EventType]] = {
    val uri         = baseUri / "event-types" / name.name
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[Option[EventType]](request) {
                     case Status.NotFound(_)   => None.pure[IO]
                     case Status.Successful(l) => l.as[EventType].map(_.some)
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }

  override def update(name: EventTypeName, eventType: EventType)(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types" / name.name
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](PUT, uri, headers = Headers(headers)).withEntity(eventType)
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[Unit](request) {
                     case Status.Successful(_) => ().pure[IO]
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }

  override def delete(name: EventTypeName)(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types" / name.name
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](DELETE, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[Unit](request) {
                     case Status.Successful(_) => ().pure[IO]
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }
}
