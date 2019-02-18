package de.zalando.fs2.nakadi.api

import cats.data.Kleisli
import cats.effect.IO
import cats.tagless.finalAlg

import de.zalando.fs2.nakadi.impl.Events
import de.zalando.fs2.nakadi.model.{Event, EventTypeName, FlowId, NakadiConfig}
import io.circe.Encoder

@finalAlg
trait EventApi[F[_]] {
  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(
      implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, Unit]]
}

object EventApi {
  implicit object ioEvent extends EventApi[IO] {
    val impl = new Events[IO](httpClient)

    def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, Unit]] =
      impl.publish(name, events)
  }
}
