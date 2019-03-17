package fs2.nakadi.model

sealed trait EventCallback[T]

final case class EventCallbackData[T](subscriptionEvent: SubscriptionEvent[T], streamId: StreamId, flowId: FlowId)

object EventCallback {

  final case class successAlways[T](eventCallback: EventCallbackData[T] => Unit) extends EventCallback[T]

  final case class successPredicate[T](eventCallback: EventCallbackData[T] => Boolean) extends EventCallback[T]

}
