package fs2.nakadi.api
import cats.effect.IO
import fs2.nakadi.Util._
import fs2.nakadi.dsl.Registries
import fs2.nakadi.model.{EnrichmentStrategy, FlowId, NakadiConfig, PartitionStrategy}

object RegistryApi {
  def enrichmentStrategies(implicit config: NakadiConfig,
                           flowId: FlowId = randomFlowId()): IO[List[EnrichmentStrategy]] =
    Registries.enrichmentStrategies.foldMap(Registries.compiler)

  def partitionStrategies(implicit config: NakadiConfig, flowId: FlowId = randomFlowId()): IO[List[PartitionStrategy]] =
    Registries.partitionStrategies.foldMap(Registries.compiler)
}
