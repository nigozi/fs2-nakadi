package fs2.nakadi.interpreters
import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.nakadi.Implicits
import fs2.nakadi.dsl.HttpClient
import fs2.nakadi.error.ServerError
import fs2.nakadi.model.{EnrichmentStrategy, FlowId, NakadiConfig, PartitionStrategy}
import org.http4s.dsl.io.GET
import org.http4s.{Request, Status, Uri}

class RegistryInterpreter[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable])
    extends Implicits
    with HttpClient {

  def enrichmentStrategies(implicit config: NakadiConfig[F], flowId: FlowId): F[List[EnrichmentStrategy]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "registry" / "enrichment-strategies"
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[List[EnrichmentStrategy]](request) {
                   case Status.Successful(l) => l.as[List[EnrichmentStrategy]]
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }

  def partitionStrategies(implicit config: NakadiConfig[F], flowId: FlowId): F[List[PartitionStrategy]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "registry" / "partition-strategies"
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[List[PartitionStrategy]](request) {
                   case Status.Successful(l) => l.as[List[PartitionStrategy]]
                   case r                    => r.as[String].flatMap(e => ME.raiseError(ServerError(r.status.code, e)))
                 }
    } yield response
  }
}

object RegistryInterpreter {
  def apply[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable]): RegistryInterpreter[F] =
    new RegistryInterpreter()
}
