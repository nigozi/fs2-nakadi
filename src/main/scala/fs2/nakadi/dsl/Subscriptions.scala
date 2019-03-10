package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.Stream
import fs2.nakadi.Implicits._
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

  def eventStream[T](subscriptionId: SubscriptionId, streamConfig: StreamConfig)(
      implicit config: NakadiConfig[F],
      decoder: Decoder[T]): Stream[F, StreamEvent[T]]

  def eventStreamManaged[T](parallelism: Int)(
      subscriptionId: SubscriptionId,
      eventCallback: EventCallback[T],
      streamConfig: StreamConfig)(implicit config: NakadiConfig[F], decoder: Decoder[T]): Stream[F, Boolean]
}

object Subscriptions {
  implicit object ioInterpreter extends SubscriptionInterpreter[IO](httpClient[IO])
}
