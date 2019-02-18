package de.zalando.fs2.nakadi.impl
import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}

import de.zalando.fs2.nakadi.model._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.Client

import io.circe.Encoder

class Events[F[_]: Monad: Sync](client: Client[F]) {

  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[Events[F]])

  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])
    (implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, Unit]] =
    Kleisli { config =>
      val uri         = config.baseUri / "event-types" / name.name / "events"
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, config.oAuth2TokenProvider)
        request = Request[F](POST, uri, headers = Headers(headers)).withEntity(events)
        _       = logger.debug(request.toString)
        response <- httpClient(config, client).fetch[Either[String, Unit]](request) {
          case Status.Successful(_) => Monad[F].pure(().asRight)
          case r                    => r.as[String].map(_.asLeft)
        }
      } yield response
    }
}
