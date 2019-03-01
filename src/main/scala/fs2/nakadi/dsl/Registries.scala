package fs2.nakadi.dsl

import cats.effect.IO
import cats.free.Free
import cats.free.Free.liftF
import cats.~>
import fs2.nakadi.Implicits
import fs2.nakadi.Util._
import fs2.nakadi.error.ServerError
import fs2.nakadi.model.{EnrichmentStrategy, FlowId, NakadiConfig, PartitionStrategy}
import org.http4s.dsl.io._
import org.http4s.{Request, Uri}

sealed trait RegistryA[T]
case class GetEnrichmentStrategies(config: NakadiConfig, flowId: FlowId) extends RegistryA[List[EnrichmentStrategy]]
case class GetPartitionStrategies(config: NakadiConfig, flowId: FlowId)  extends RegistryA[List[PartitionStrategy]]

object Registries extends Implicits with HttpClient {
  type RegistryF[A] = Free[RegistryA, A]

  def enrichmentStrategies(implicit config: NakadiConfig,
                           flowId: FlowId = randomFlowId()): RegistryF[List[EnrichmentStrategy]] =
    liftF[RegistryA, List[EnrichmentStrategy]](GetEnrichmentStrategies(config, flowId))

  def partitionStrategies(implicit config: NakadiConfig,
                          flowId: FlowId = randomFlowId()): RegistryF[List[PartitionStrategy]] =
    liftF[RegistryA, List[PartitionStrategy]](GetPartitionStrategies(config, flowId))

  def compiler: RegistryA ~> IO =
    new (RegistryA ~> IO) {
      override def apply[A](fa: RegistryA[A]): IO[A] =
        fa match {
          case GetEnrichmentStrategies(config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "registry" / "enrichment-strategies"
            val request              = Request[IO](GET, uri)

            fetch[List[EnrichmentStrategy]](request, config)(r =>
              r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
          case GetPartitionStrategies(config, flowId) =>
            implicit val fid: FlowId = flowId
            val uri                  = Uri.unsafeFromString(config.uri.toString) / "registry" / "partition-strategies"
            val request              = Request[IO](GET, uri)

            fetch[List[PartitionStrategy]](request, config)(r =>
              r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e))))
        }
    }
}
