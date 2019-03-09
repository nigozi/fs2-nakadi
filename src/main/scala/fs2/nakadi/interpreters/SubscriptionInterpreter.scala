package fs2.nakadi.interpreters
import cats.effect.{Async, ContextShift}
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Monad, MonadError}
import fs2.Stream
import fs2.nakadi.model._
import io.circe.{Decoder, Json}
import jawnfs2._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{Header, Request, Status, Uri}
import org.typelevel.jawn.RawFacade

class SubscriptionInterpreter[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable], M: Monad[F])
    extends HttpClient {

  def create(subscription: Subscription)(implicit config: NakadiConfig[F]): F[Subscription] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "subscriptions"
    val req        = Request[F](POST, uri).withEntity(encode(subscription))
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Subscription](request) {
                   case Status.Successful(l) => l.as[Subscription]
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def createIfDoesntExist(subscription: Subscription)(implicit config: NakadiConfig[F]): F[Subscription] = {
    for {
      subscriptions <- list(Some(subscription.owningApplication), subscription.eventTypes)
      collect = subscriptions.items.filter { returningSubscription =>
        val consumerGroupCheck = subscription.consumerGroup match {
          case None => true
          case consumerGroup =>
            returningSubscription.consumerGroup == consumerGroup
        }

        val idCheck = subscription.id match {
          case None => true
          case id =>
            returningSubscription.id == id
        }

        consumerGroupCheck && idCheck
      }

      createIfEmpty <- collect.headOption.map(M.pure).getOrElse(create(subscription))

    } yield createIfEmpty
  }

  def list(owningApplication: Option[String] = None,
           eventType: Option[List[EventTypeName]] = None,
           limit: Option[Int] = None,
           offset: Option[Int] = None)(implicit config: NakadiConfig[F]): F[SubscriptionQuery] = {
    val uri = Uri
      .unsafeFromString(s"${config.uri.toString}/subscriptions")
      .withOptionQueryParam("owning_application", owningApplication)
      .withOptionQueryParam("limit", limit)
      .withOptionQueryParam("offset", offset)

    val uriWithEventType = eventType.map(tp => uri.withQueryParam("event_type", tp.map(_.name))).getOrElse(uri)

    val req        = Request[F](GET, uriWithEventType)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[SubscriptionQuery](request) {
                   case Status.Successful(l) => l.as[SubscriptionQuery]
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def get(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F]): F[Option[Subscription]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Option[Subscription]](request) {
                   case Status.NotFound(_)   => M.pure(None)
                   case Status.Successful(l) => l.as[Subscription].map(_.some)
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def delete(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F]): F[Unit] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString
    val req        = Request[F](DELETE, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(l) => M.pure(())
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def cursors(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F]): F[Option[SubscriptionCursor]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "cursors"
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Option[SubscriptionCursor]](request) {
                   case Status.NotFound(_) | Status.NoContent(_) => M.pure(None)
                   case Status.Successful(l)                     => l.as[SubscriptionCursor].map(_.some)
                   case r                                        => throwServerError(r)
                 }
    } yield response
  }

  def commitCursors(subscriptionId: SubscriptionId, subscriptionCursor: SubscriptionCursor, streamId: StreamId)(
      implicit config: NakadiConfig[F]): F[Option[CommitCursorResponse]] = {
    val uri          = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "cursors"
    val req          = Request[F](POST, uri).withEntity(encode(subscriptionCursor))
    val httpClient   = config.httpClient.getOrElse(defaultClient[F])
    val streamHeader = Header(xNakadiStreamIdHeader, streamId.id)

    for {
      request <- addBaseHeaders(req, config, List(streamHeader))
      response <- httpClient.fetch[Option[CommitCursorResponse]](request) {
                   case Status.NotFound(_) | Status.NoContent(_) => M.pure(None)
                   case Status.Successful(l)                     => l.as[CommitCursorResponse].map(_.some)
                   case r                                        => throwServerError(r)
                 }
    } yield response
  }

  def resetCursors(subscriptionId: SubscriptionId, subscriptionCursor: Option[SubscriptionCursor] = None)(
      implicit config: NakadiConfig[F]): F[Boolean] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "cursors"
    val req = subscriptionCursor match {
      case Some(c) => Request[F](PATCH, uri).withEntity(encode(c))
      case None    => Request[F](PATCH, uri)
    }
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[Boolean](request) {
                   case Status.NotFound(_) | Status.NoContent(_) => M.pure(false)
                   case Status.Successful(_)                     => M.pure(true)
                   case r                                        => throwServerError(r)
                 }
    } yield response
  }

  def eventStream[T](subscriptionId: SubscriptionId, eventCallback: EventCallback[T], streamConfig: StreamConfig)(
      implicit config: NakadiConfig[F],
      decoder: Decoder[T]): Stream[F, SubscriptionEvent[T]] = {
    implicit val f: RawFacade[Json] = io.circe.jawn.CirceSupportParser.facade

    val uri = Uri
      .unsafeFromString(s"${config.uri.toString}/subscriptions/${subscriptionId.id.toString}/events")
      .withOptionQueryParam("max_uncommitted_events", streamConfig.maxUncommittedEvents)
      .withOptionQueryParam("batch_limit", streamConfig.batchLimit)
      .withOptionQueryParam("stream_limit", streamConfig.streamLimit)
      .withOptionQueryParam("batch_flush_timeout", streamConfig.batchFlushTimeout.map(_.toSeconds))
      .withOptionQueryParam("stream_timeout", streamConfig.streamTimeout.map(_.toSeconds))
      .withOptionQueryParam("stream_keep_alive_limit", streamConfig.streamKeepAliveLimit)
      .withOptionQueryParam("commit_timeout", streamConfig.commitTimeout.map(_.toSeconds))

    val httpClient = config.httpClient.getOrElse(defaultClient[F])
    val request    = addBaseHeaders(Request[F](GET, uri), config)

    Stream
      .eval(request)
      .flatMap(
        req =>
          httpClient
            .stream(req)
            .flatMap(_.body.chunks.parseJsonStream)
            .map(_.as[SubscriptionEvent[T]].valueOr(e => sys.error(s"Failed to decode the event: ${e.message}"))))
  }
}

object SubscriptionInterpreter {
  def apply[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable], M: Monad[F]): SubscriptionInterpreter[F] =
    new SubscriptionInterpreter[F]()
}