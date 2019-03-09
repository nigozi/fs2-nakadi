package fs2.nakadi.dsl

import cats.effect.IO
import cats.tagless.finalAlg
import fs2.nakadi.interpreters.RegistryInterpreter
import fs2.nakadi.model.{EnrichmentStrategy, NakadiConfig, PartitionStrategy}
import Implicits._

@finalAlg
trait Registries[F[_]] {
  def enrichmentStrategies(implicit config: NakadiConfig[F]): F[List[EnrichmentStrategy]]

  def partitionStrategies(implicit config: NakadiConfig[F]): F[List[PartitionStrategy]]
}

object Registries {
  implicit object ioInterpreter extends RegistryInterpreter[IO]
}
