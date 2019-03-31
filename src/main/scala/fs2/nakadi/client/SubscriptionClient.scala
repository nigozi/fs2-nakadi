package fs2.nakadi.client
import cats.MonadError
import cats.effect.{Async, Concurrent, ContextShift}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import fs2.nakadi.dsl.SubscriptionDsl
import fs2.nakadi.error.ExpectedHeader
import fs2.nakadi.implicits._
import fs2.nakadi.model._
import fs2.nakadi.{EventCallback, httpClient}
import fs2.{Chunk, Pipe, Stream}
import io.circe.{Decoder, DecodingFailure, Json}
import jawnfs2._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, Request, Response, Status, Uri}
import org.typelevel.jawn.RawFacade

import scala.util.{Failure, Success, Try}

class SubscriptionClient[F[_]: Async: ContextShift: Concurrent](httpClient: Client[F])(implicit config: NakadiConfig[F],
                                                                                       M: MonadError[F, Throwable])
    extends SubscriptionDsl[F] {
  import SubscriptionClient._

  private val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[SubscriptionClient[F]])

  implicit val f: RawFacade[Json] = io.circe.jawn.CirceSupportParser.facade

  override def create(subscription: Subscription)(implicit flowId: FlowId): F[Subscription] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions"
    val req = Request[F](POST, uri).withEntity(encode(subscription))

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Subscription](request) {
                   case Status.Successful(l) => l.as[Subscription]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def createIfDoesntExist(subscription: Subscription)(implicit flowId: FlowId): F[Subscription] = {
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

  override def list(owningApplication: Option[String] = None,
                    eventType: Option[List[EventTypeName]] = None,
                    limit: Option[Int] = None,
                    offset: Option[Int] = None)(implicit flowId: FlowId): F[SubscriptionQuery] = {
    val uri = Uri
      .unsafeFromString(s"${config.uri.toString}/subscriptions")
      .withOptionQueryParam("owning_application", owningApplication)
      .withOptionQueryParam("limit", limit)
      .withOptionQueryParam("offset", offset)

    val uriWithEventType = eventType.map(tp => uri.withQueryParam("event_type", tp.map(_.name))).getOrElse(uri)

    val req = Request[F](GET, uriWithEventType)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[SubscriptionQuery](request) {
                   case Status.Successful(l) => l.as[SubscriptionQuery]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def get(subscriptionId: SubscriptionId)(implicit flowId: FlowId): F[Option[Subscription]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Option[Subscription]](request) {
                   case Status.NotFound(_)   => M.pure(None)
                   case Status.Successful(l) => l.as[Subscription].map(_.some)
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def delete(subscriptionId: SubscriptionId)(implicit flowId: FlowId): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString
    val req = Request[F](DELETE, uri)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Unit](request) {
                   case Status.Successful(_) => M.pure(())
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def cursors(subscriptionId: SubscriptionId)(implicit flowId: FlowId): F[Option[SubscriptionCursor]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "cursors"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Option[SubscriptionCursor]](request) {
                   case Status.NotFound(_) | Status.NoContent(_) => M.pure(None)
                   case Status.Successful(l)                     => l.as[SubscriptionCursor].map(_.some)
                   case r                                        => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def commitCursors(subscriptionId: SubscriptionId,
                             subscriptionCursor: SubscriptionCursor,
                             streamId: StreamId)(implicit flowId: FlowId): F[Option[CommitCursorResponse]] = {
    val uri          = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "cursors"
    val req          = Request[F](POST, uri).withEntity(encode(subscriptionCursor))
    val streamHeader = Header(XNakadiStreamId, streamId.id)

    for {
      request <- addHeaders(req, List(streamHeader))
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Option[CommitCursorResponse]](request) {
                   case Status.NoContent(_) => M.pure(None)
                   case Status.Successful(l) =>
                     logger.warn(
                       s"SubscriptionId: ${subscriptionId.id.toString}, StreamId: ${streamId.id} At least one cursor failed to commit")
                     l.as[CommitCursorResponse].map(_.some)
                   case r => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def resetCursors(subscriptionId: SubscriptionId, subscriptionCursor: Option[SubscriptionCursor] = None)(
      implicit flowId: FlowId): F[Unit] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "cursors"
    val req = subscriptionCursor match {
      case Some(c) => Request[F](PATCH, uri).withEntity(encode(c))
      case None    => Request[F](PATCH, uri)
    }

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Unit](request) {
                   case Status.NoContent(_) => M.pure(())
                   case r                   => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def stats(subscriptionId: SubscriptionId)(implicit flowId: FlowId): F[Option[SubscriptionStats]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "subscriptions" / subscriptionId.id.toString / "stats"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[Option[SubscriptionStats]](request) {
                   case Status.NotFound(_)   => M.pure(None)
                   case Status.Successful(l) => l.as[SubscriptionStats].map(_.some)
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def eventStream[T: Decoder](subscriptionId: SubscriptionId, streamConfig: StreamConfig)(
      implicit flowId: FlowId): Stream[F, StreamEvent[T]] =
    connect[T](subscriptionId, streamConfig)

  override def managedEventStream[T: Decoder](parallelism: Int)(
      subscriptionId: SubscriptionId,
      eventCallback: EventCallback[T],
      streamConfig: StreamConfig)(implicit flowId: FlowId): Stream[F, Boolean] =
    connect[T](subscriptionId, streamConfig)
      .through(processEvents(parallelism)(subscriptionId, eventCallback))

  private def connect[T: Decoder](subscriptionId: SubscriptionId, streamConfig: StreamConfig)(
      implicit flowId: FlowId): Stream[F, StreamEvent[T]] = {
    val uri = Uri
      .unsafeFromString(s"${config.uri.toString}/subscriptions/${subscriptionId.id.toString}/events")
      .withOptionQueryParam("max_uncommitted_events", streamConfig.maxUncommittedEvents)
      .withOptionQueryParam("batch_limit", streamConfig.batchLimit)
      .withOptionQueryParam("stream_limit", streamConfig.streamLimit)
      .withOptionQueryParam("batch_flush_timeout", streamConfig.batchFlushTimeout.map(_.toSeconds))
      .withOptionQueryParam("stream_timeout", streamConfig.streamTimeout.map(_.toSeconds))
      .withOptionQueryParam("stream_keep_alive_limit", streamConfig.streamKeepAliveLimit)
      .withOptionQueryParam("commit_timeout", streamConfig.commitTimeout.map(_.toSeconds))

    val request = for {
      req <- addHeaders(Request[F](GET, uri), List(Connection(CaseInsensitiveString("keep-alive"))))
      _   = logger.debug(req.toString())
    } yield req

    Stream
      .eval(request)
      .flatMap(httpClient.stream)
      .flatMap(processStreamResponse[T](subscriptionId))
      .handleErrorWith {
        case NoEmptySlotsOrCursorReset(_) =>
          logger.error("No empty slots or cursor reset, restarting")
          Thread.sleep(streamConfig.noEmptySlotsRetryDelay.toMillis)
          connect[T](subscriptionId, streamConfig)
        case e =>
          logger.error("stream failure", e)
          Stream.raiseError(e)
      }
  }

  private def processStreamResponse[T: Decoder](subscriptionId: SubscriptionId)(
      resp: Response[F]): Stream[F, StreamEvent[T]] =
    resp.status match {
      case s if s.isSuccess =>
        val streamId = resp.headers
          .get(CaseInsensitiveString(XNakadiStreamId))
          .map(h => StreamId(h.value))
          .getOrElse(throw ExpectedHeader(XNakadiStreamId))

        resp.body.chunks.through(parseChunks[T]).flatMap {
          case Right(se) => Stream.eval(M.pure(StreamEvent(se, streamId)))
          case Left(e)   => throw e
        }
      case Status.NotFound => throw SubscriptionNotFound(s"subscription $subscriptionId not found")
      case Status.Conflict =>
        throw NoEmptySlotsOrCursorReset(s"no empty slot for the subscription $subscriptionId")
      case _ => Stream.eval(unsuccessfulOperation(resp))
    }

  private def parseChunks[T: Decoder]: Pipe[F, Chunk[Byte], Either[DecodingFailure, SubscriptionEvent[T]]] = { stream =>
    stream.parseJsonStream
      .map(_.as[SubscriptionEvent[T]])
  }

  private def processEvents[T](parallelism: Int)(subscriptionId: SubscriptionId, callback: EventCallback[T])(
      implicit flowId: FlowId): Pipe[F, StreamEvent[T], Boolean] = { stream =>
    stream
      .mapAsync(parallelism) { se =>
        Try(callback(EventCallbackData(se.event, se.streamId, flowId))) match {
          case Success(true) =>
            commitCursors(subscriptionId, SubscriptionCursor(List(se.event.cursor)), se.streamId)
              .map(_ => true)
              .handleError { e =>
                logger.error("Commit cursor failed", e)
                false
              }
          case Success(false) =>
            logger.debug("Event callback returned false, not committing cursors")
            M.pure(false)
          case Failure(e) =>
            logger.error("Event callback failed, not committing cursors", e)
            M.pure(false)
        }
      }
  }
}

object SubscriptionClient {
  def apply[F[_]: Async: ContextShift: Concurrent](implicit config: NakadiConfig[F]): SubscriptionDsl[F] =
    new SubscriptionClient(httpClient[F])

  final case class NoEmptySlotsOrCursorReset(message: String) extends Throwable
  final case class SubscriptionNotFound(message: String)      extends Throwable
}
