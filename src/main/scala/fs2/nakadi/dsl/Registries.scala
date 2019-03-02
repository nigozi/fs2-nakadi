package fs2.nakadi.dsl

import cats.effect.IO
import cats.tagless.finalAlg
import fs2.nakadi.Implicits
import fs2.nakadi.interpreters.RegistryInterpreter
import fs2.nakadi.model.{EnrichmentStrategy, FlowId, NakadiConfig, PartitionStrategy}

@finalAlg
trait Registries[F[_]] {
  def enrichmentStrategies(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId): F[List[EnrichmentStrategy]]

  def partitionStrategies(implicit config: NakadiConfig[F], flowId: FlowId = randomFlowId): F[List[PartitionStrategy]]
}

object Registries {
  implicit object ioInterpreter extends Registries[IO] with Implicits {

    override def enrichmentStrategies(implicit config: NakadiConfig[IO], flowId: FlowId): IO[List[EnrichmentStrategy]] =
      RegistryInterpreter[IO].enrichmentStrategies

    override def partitionStrategies(implicit config: NakadiConfig[IO], flowId: FlowId): IO[List[PartitionStrategy]] =
      RegistryInterpreter[IO].partitionStrategies
  }
}
