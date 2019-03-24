package fs2.nakadi.interpreters
import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import fs2.nakadi.dsl.Registries
import fs2.nakadi.implicits._
import fs2.nakadi.model.{EnrichmentStrategy, FlowId, NakadiConfig, PartitionStrategy}
import org.http4s.client.Client
import org.http4s.dsl.io.GET
import org.http4s.{Request, Status, Uri}

class RegistryInterpreter[F[_]: Async: ContextShift](httpClient: Client[F])(implicit ME: MonadError[F, Throwable])
    extends Registries[F]
    with Interpreter {
  private val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[RegistryInterpreter[F]])

  override def enrichmentStrategies(implicit config: NakadiConfig[F], flowId: FlowId): F[List[EnrichmentStrategy]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "registry" / "enrichment-strategies"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[List[EnrichmentStrategy]](request) {
                   case Status.Successful(l) => l.as[List[EnrichmentStrategy]]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def partitionStrategies(implicit config: NakadiConfig[F], flowId: FlowId): F[List[PartitionStrategy]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "registry" / "partition-strategies"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _ = logger.debug(request.toString())
      response <- httpClient.fetch[List[PartitionStrategy]](request) {
                   case Status.Successful(l) => l.as[List[PartitionStrategy]]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }
}
