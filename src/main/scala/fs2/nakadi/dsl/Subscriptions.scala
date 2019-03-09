package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.Stream
import fs2.nakadi.interpreters.SubscriptionInterpreter
import fs2.nakadi.model._
import io.circe.Decoder

@finalAlg
trait Subscriptions[F[_]] {
  def create(subscription: Subscription)(implicit config: NakadiConfig[F]): F[Subscription]

  def createIfDoesntExist(subscription: Subscription)(implicit config: NakadiConfig[F]): F[Subscription]

  def list(owningApplication: Option[String] = None,
           eventType: Option[List[EventTypeName]] = None,
           limit: Option[Int] = None,
           offset: Option[Int] = None)(implicit config: NakadiConfig[F]): F[SubscriptionQuery]

  def get(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F]): F[Option[Subscription]]

  def delete(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F]): F[Unit]

  def cursors(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F]): F[Option[SubscriptionCursor]]

  def commitCursors(subscriptionId: SubscriptionId, subscriptionCursor: SubscriptionCursor, streamId: StreamId)(
      implicit config: NakadiConfig[F]): F[Option[CommitCursorResponse]]

  def resetCursors(subscriptionId: SubscriptionId, subscriptionCursor: Option[SubscriptionCursor] = None)(
      implicit config: NakadiConfig[F]): F[Boolean]

  def eventStream[T](subscriptionId: SubscriptionId, eventCallback: EventCallback[T], streamConfig: StreamConfig)(
      implicit config: NakadiConfig[F],
      decoder: Decoder[T]): Stream[F, SubscriptionEvent[T]]
}

object Subscriptions {
  implicit object ioInterpreter extends Subscriptions[IO] with Implicits {
    override def create(subscription: Subscription)(implicit config: NakadiConfig[IO]): IO[Subscription] =
      SubscriptionInterpreter[IO].create(subscription)

    override def createIfDoesntExist(subscription: Subscription)(implicit config: NakadiConfig[IO]): IO[Subscription] =
      SubscriptionInterpreter[IO].createIfDoesntExist(subscription)

    override def list(owningApplication: Option[String],
                      eventType: Option[List[EventTypeName]],
                      limit: Option[Int],
                      offset: Option[Int])(implicit config: NakadiConfig[IO]): IO[SubscriptionQuery] =
      SubscriptionInterpreter[IO].list(owningApplication, eventType, limit, offset)

    override def get(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[IO]): IO[Option[Subscription]] =
      SubscriptionInterpreter[IO].get(subscriptionId)

    override def delete(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[IO]): IO[Unit] =
      SubscriptionInterpreter[IO].delete(subscriptionId)

    override def cursors(subscriptionId: SubscriptionId)(
        implicit config: NakadiConfig[IO]): IO[Option[SubscriptionCursor]] =
      SubscriptionInterpreter[IO].cursors(subscriptionId)

    override def commitCursors(
        subscriptionId: SubscriptionId,
        subscriptionCursor: SubscriptionCursor,
        streamId: StreamId)(implicit config: NakadiConfig[IO]): IO[Option[CommitCursorResponse]] =
      SubscriptionInterpreter[IO].commitCursors(subscriptionId, subscriptionCursor, streamId)

    override def resetCursors(subscriptionId: SubscriptionId, subscriptionCursor: Option[SubscriptionCursor])(
        implicit config: NakadiConfig[IO]): IO[Boolean] =
      SubscriptionInterpreter[IO].resetCursors(subscriptionId, subscriptionCursor)

    def eventStream[T](subscriptionId: SubscriptionId, eventCallback: EventCallback[T], streamConfig: StreamConfig)(
        implicit config: NakadiConfig[IO],
        decoder: Decoder[T]): Stream[IO, SubscriptionEvent[T]] =
      SubscriptionInterpreter[IO].eventStream(subscriptionId, eventCallback, streamConfig)
  }
}
