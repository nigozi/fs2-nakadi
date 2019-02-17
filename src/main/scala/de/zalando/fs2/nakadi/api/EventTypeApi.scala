package de.zalando.fs2.nakadi.api
import java.net.URI

import cats.effect.IO
import de.zalando.fs2.nakadi.impl.EventTypes
import de.zalando.fs2.nakadi.model._
import org.http4s.Uri

trait EventTypeApi[F[_]] {
  def list()(implicit flowId: FlowId = randomFlowId()): F[Either[String, List[EventType]]]

  def create(eventType: EventType)(implicit flowId: FlowId = randomFlowId()): F[Either[String, Unit]]

  def get(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Either[String, Option[EventType]]]

  def update(name: EventTypeName, eventType: EventType)(
      implicit flowId: FlowId = randomFlowId()): F[Either[String, Unit]]

  def delete(name: EventTypeName)(implicit flowId: FlowId = randomFlowId()): F[Either[String, Unit]]
}

object EventTypeApi {
  def ioInterpreter(uri: URI, oAuth2TokenProvider: Option[OAuth2TokenProvider[IO]] = None): EventTypeApi[IO] =
    new EventTypeApi[IO] {
      val impl = new EventTypes[IO](Uri.unsafeFromString(uri.toString), oAuth2TokenProvider)

      override def list()(implicit flowId: FlowId): IO[Either[String, List[EventType]]] =
        impl.list().run(httpClient)
      override def create(eventType: EventType)(implicit flowId: FlowId): IO[Either[String, Unit]] =
        impl.create(eventType).run(httpClient)
      override def get(name: EventTypeName)(implicit flowId: FlowId): IO[Either[String, Option[EventType]]] =
        impl.get(name).run(httpClient)
      override def update(name: EventTypeName, eventType: EventType)(
          implicit flowId: FlowId): IO[Either[String, Unit]] =
        impl.update(name, eventType).run(httpClient)
      override def delete(name: EventTypeName)(implicit flowId: FlowId): IO[Either[String, Unit]] =
        impl.delete(name).run(httpClient)
    }
}
