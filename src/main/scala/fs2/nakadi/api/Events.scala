package fs2.nakadi.api

import cats.effect.IO
import cats.syntax.applicative._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.http4s.Method.POST

import fs2.nakadi.error.{BatchItemResponse, EventValidation, _}
import fs2.nakadi.model._
import io.circe.Encoder

trait EventAlg[F[_]] {
  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(
      implicit flowId: FlowId = randomFlowId()): F[Unit]
}

class Events(config: NakadiConfig) extends EventAlg[IO] with Implicits {
  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[Events])

  private val baseUri       = Uri.unsafeFromString(config.uri.toString)
  private val tokenProvider = config.tokenProvider
  private val httpClient    = config.httpClient.getOrElse(defaultClient)

  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(implicit flowId: FlowId): IO[Unit] = {
    val uri         = baseUri / "event-types" / name.name / "events"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](POST, uri, headers = Headers(headers)).withEntity(events)
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[Unit](request) {
                     case Status.Successful(_) => ().pure[IO]
                     case Status.UnprocessableEntity(r) =>
                       r.as[List[BatchItemResponse]].flatMap(e => IO.raiseError(EventValidation(e)))
                     case r => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }
}
