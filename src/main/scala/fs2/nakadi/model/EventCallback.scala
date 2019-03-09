package fs2.nakadi.model

sealed abstract class EventCallback[T](val separateFlowId: Boolean)

final case class EventCallbackData[T](subscriptionEvent: SubscriptionEvent[T],
                                      streamId: StreamId,
                                      flowId: Option[FlowId])

object EventCallback {

  final case class simple[T](eventCallback: EventCallbackData[T] => Unit, override val separateFlowId: Boolean = true)
      extends EventCallback[T](separateFlowId)

  final case class successAlways[T](eventCallback: EventCallbackData[T] => Unit,
                                    override val separateFlowId: Boolean = true)
      extends EventCallback[T](separateFlowId)

  final case class successPredicate[T](eventCallback: EventCallbackData[T] => Boolean,
                                       override val separateFlowId: Boolean = true)
      extends EventCallback[T](separateFlowId)

}
