package fs2.nakadi.model

final case class EventCallbackData[T](subscriptionEvent: SubscriptionEvent[T], streamId: StreamId, flowId: FlowId)
