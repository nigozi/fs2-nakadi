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

class EventTypes[F[_]: Monad: Sync](baseUri: Uri, oAuth2TokenProvider: Option[OAuth2TokenProvider[F]] = None) {

  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[EventTypes[F]])

  def list()(implicit flowId: FlowId): Kleisli[F, Client[F], Either[String, List[EventType]]] =
    Kleisli { client =>
      val uri         = baseUri / "event-types"
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, oAuth2TokenProvider)
        request = Request[F](GET, uri, headers = Headers(headers))
        _       = logger.debug(request.toString)
        response <- client.fetch[Either[String, List[EventType]]](request) {
                     case Status.Successful(r) => r.attemptAs[List[EventType]].leftMap(_.message).value
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }

  def create(eventType: EventType)(implicit flowId: FlowId): Kleisli[F, Client[F], Either[String, Unit]] =
    Kleisli { client =>
      val uri         = baseUri / "event-types"
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, oAuth2TokenProvider)
        request = Request[F](POST, uri, headers = Headers(headers)).withEntity(eventType)
        _       = logger.debug(request.toString)
        response <- client.fetch[Either[String, Unit]](request) {
                     case Status.Successful(_) => Monad[F].pure(().asRight)
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }

  def get(name: EventTypeName)(implicit flowId: FlowId): Kleisli[F, Client[F], Either[String, Option[EventType]]] =
    Kleisli { client =>
      val uri         = baseUri / "event-types" / name.name
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, oAuth2TokenProvider)
        request = Request[F](GET, uri, headers = Headers(headers))
        _       = logger.debug(request.toString)
        response <- client.fetch[Either[String, Option[EventType]]](request) {
                     case Status.NotFound(_)   => Monad[F].pure(None.asRight)
                     case Status.Successful(r) => r.attemptAs[EventType].leftMap(_.message).map(_.some).value
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }

  def update(name: EventTypeName, eventType: EventType)(
      implicit flowId: FlowId): Kleisli[F, Client[F], Either[String, Unit]] =
    Kleisli { client =>
      val uri         = baseUri / "event-types" / name.name
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, oAuth2TokenProvider)
        request = Request[F](PUT, uri, headers = Headers(headers)).withEntity(eventType)
        _       = logger.debug(request.toString)
        response <- client.fetch[Either[String, Unit]](request) {
                     case Status.Successful(_) => Monad[F].pure(().asRight)
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }

  def delete(name: EventTypeName)(implicit flowId: FlowId): Kleisli[F, Client[F], Either[String, Unit]] =
    Kleisli { client =>
      val uri         = baseUri / "event-types" / name.name
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, oAuth2TokenProvider)
        request = Request[F](DELETE, uri, headers = Headers(headers))
        _       = logger.debug(request.toString)
        response <- client.fetch[Either[String, Unit]](request) {
                     case Status.Successful(_) => Monad[F].pure(().asRight)
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }
}
