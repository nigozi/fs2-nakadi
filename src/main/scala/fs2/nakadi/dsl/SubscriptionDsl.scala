package fs2.nakadi.dsl
import fs2.Stream
import fs2.nakadi.model._
import fs2.nakadi.{EventCallback, randomFlowId}
import io.circe.Decoder

trait SubscriptionDsl[F[_]] {

  def create(subscription: Subscription)(implicit flowId: FlowId = randomFlowId()): F[Subscription]

  def createIfDoesntExist(subscription: Subscription)(implicit flowId: FlowId = randomFlowId()): F[Subscription]

  def list(owningApplication: Option[String] = None,
           eventType: Option[List[EventTypeName]] = None,
           limit: Option[Int] = None,
           offset: Option[Int] = None)(implicit flowId: FlowId = randomFlowId()): F[SubscriptionQuery]

  def get(subscriptionId: SubscriptionId)(implicit flowId: FlowId = randomFlowId()): F[Option[Subscription]]

  def delete(subscriptionId: SubscriptionId)(implicit flowId: FlowId = randomFlowId()): F[Unit]

  def stats(subscriptionId: SubscriptionId)(implicit flowId: FlowId = randomFlowId()): F[Option[SubscriptionStats]]

  def cursors(subscriptionId: SubscriptionId)(implicit flowId: FlowId = randomFlowId()): F[Option[SubscriptionCursor]]

  def commitCursors(subscriptionId: SubscriptionId, subscriptionCursor: SubscriptionCursor, streamId: StreamId)(
      implicit flowId: FlowId = randomFlowId()): F[Option[CommitCursorResponse]]

  def resetCursors(subscriptionId: SubscriptionId, subscriptionCursor: Option[SubscriptionCursor] = None)(
      implicit flowId: FlowId = randomFlowId()): F[Unit]

  def eventStream[T: Decoder](subscriptionId: SubscriptionId, streamConfig: StreamConfig)(
      implicit flowId: FlowId = randomFlowId()): Stream[F, StreamEvent[T]]

  def managedEventStream[T: Decoder](parallelism: Int)(
      subscriptionId: SubscriptionId,
      eventCallback: EventCallback[T],
      streamConfig: StreamConfig)(implicit flowId: FlowId = randomFlowId()): Stream[F, Boolean]
}
