package fs2.nakadi.dsl

import fs2.nakadi.model.{EnrichmentStrategy, FlowId, PartitionStrategy}
import fs2.nakadi.randomFlowId

trait RegistryDsl[F[_]] {
  def enrichmentStrategies(implicit flowId: FlowId = randomFlowId()): F[List[EnrichmentStrategy]]

  def partitionStrategies(implicit flowId: FlowId = randomFlowId()): F[List[PartitionStrategy]]
}
