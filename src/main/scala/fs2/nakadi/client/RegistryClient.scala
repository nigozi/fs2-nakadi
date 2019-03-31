package fs2.nakadi.client
import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import fs2.nakadi.dsl.RegistryDsl
import fs2.nakadi.httpClient
import fs2.nakadi.implicits._
import fs2.nakadi.model.{EnrichmentStrategy, FlowId, NakadiConfig, PartitionStrategy}
import org.http4s.client.Client
import org.http4s.dsl.io.GET
import org.http4s.{Request, Status, Uri}

class RegistryClient[F[_]: Async: ContextShift](httpClient: Client[F])(implicit config: NakadiConfig[F],
                                                                       M: MonadError[F, Throwable])
    extends RegistryDsl[F] {
  private val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[RegistryClient[F]])

  override def enrichmentStrategies(implicit flowId: FlowId): F[List[EnrichmentStrategy]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "registry" / "enrichment-strategies"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[List[EnrichmentStrategy]](request) {
                   case Status.Successful(l) => l.as[List[EnrichmentStrategy]]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }

  override def partitionStrategies(implicit flowId: FlowId): F[List[PartitionStrategy]] = {
    val uri = Uri.unsafeFromString(config.uri.toString) / "registry" / "partition-strategies"
    val req = Request[F](GET, uri)

    for {
      request <- addHeaders(req)
      _       = logger.debug(request.toString())
      response <- httpClient.fetch[List[PartitionStrategy]](request) {
                   case Status.Successful(l) => l.as[List[PartitionStrategy]]
                   case r                    => unsuccessfulOperation(r)
                 }
    } yield response
  }
}

object RegistryClient {
  def apply[F[_]: Async: ContextShift](implicit config: NakadiConfig[F]): RegistryDsl[F] =
    new RegistryClient(httpClient[F])
}
