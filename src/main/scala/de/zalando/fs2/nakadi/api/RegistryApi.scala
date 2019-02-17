package de.zalando.fs2.nakadi.api

import cats.data.Kleisli
import cats.effect.IO
import cats.tagless.finalAlg
import de.zalando.fs2.nakadi.impl.Registry
import de.zalando.fs2.nakadi.model.{FlowId, NakadiConfig, PartitionStrategy}

@finalAlg
trait RegistryApi[F[_]] {
  def enrichmentStrategies(implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, List[String]]]

  def partitionStrategies(implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, List[PartitionStrategy]]]
}

object RegistryApi {
  implicit object ioRegistry extends RegistryApi[IO] {
    val impl = new Registry[IO](httpClient)

    override def enrichmentStrategies(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, List[String]]] =
      impl.enrichmentStrategies
    override def partitionStrategies(
        implicit flowId: FlowId): Kleisli[IO, NakadiConfig[IO], Either[String, List[PartitionStrategy]]] =
      impl.partitionStrategies
  }
}
