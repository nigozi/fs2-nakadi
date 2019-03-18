package fs2.nakadi.dsl
import cats.effect.IO
import cats.tagless.finalAlg
import fs2.Stream
import fs2.nakadi.instances.ContextShifts
import fs2.nakadi.interpreters.SubscriptionInterpreter
import fs2.nakadi.model._
import fs2.nakadi.{httpClient, randomFlowId}
import io.circe.Decoder

@finalAlg
trait Subscriptions[F[_]] {
  def create(subscription: Subscription)(implicit config: NakadiConfig[F],
                                         flowId: FlowId = randomFlowId()): F[Subscription]

  def createIfDoesntExist(subscription: Subscription)(implicit config: NakadiConfig[F],
                                                      flowId: FlowId = randomFlowId()): F[Subscription]

  def list(owningApplication: Option[String] = None,
           eventType: Option[List[EventTypeName]] = None,
           limit: Option[Int] = None,
           offset: Option[Int] = None)(implicit config: NakadiConfig[F],
                                       flowId: FlowId = randomFlowId()): F[SubscriptionQuery]

  def get(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F],
                                          flowId: FlowId = randomFlowId()): F[Option[Subscription]]

  def delete(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId()): F[Unit]

  def stats(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F],
                                            flowId: FlowId = randomFlowId()): F[Option[SubscriptionStats]]

  def cursors(subscriptionId: SubscriptionId)(implicit config: NakadiConfig[F],
                                              flowId: FlowId = randomFlowId()): F[Option[SubscriptionCursor]]

  def commitCursors(subscriptionId: SubscriptionId, subscriptionCursor: SubscriptionCursor, streamId: StreamId)(
      implicit config: NakadiConfig[F],
      flowId: FlowId = randomFlowId()): F[Option[CommitCursorResponse]]

  def resetCursors(subscriptionId: SubscriptionId, subscriptionCursor: Option[SubscriptionCursor] = None)(
      implicit config: NakadiConfig[F],
      flowId: FlowId = randomFlowId()): F[Boolean]

  def eventStream[T: Decoder](subscriptionId: SubscriptionId, streamConfig: StreamConfig)(
      implicit config: NakadiConfig[F],
      flowId: FlowId = randomFlowId()): Stream[F, StreamEvent[T]]

  def managedEventStream[T: Decoder](parallelism: Int)(
      subscriptionId: SubscriptionId,
      eventCallback: EventCallback[T],
      streamConfig: StreamConfig)(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId()): Stream[F, Boolean]
}

object Subscriptions extends ContextShifts {
  implicit object ioInterpreter extends SubscriptionInterpreter[IO](httpClient[IO])
}
