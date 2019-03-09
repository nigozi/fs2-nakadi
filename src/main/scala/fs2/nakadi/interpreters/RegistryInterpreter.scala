package fs2.nakadi.interpreters
import cats.MonadError
import cats.effect.{Async, ContextShift}
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.nakadi.dsl.Registries
import fs2.nakadi.interpreters.HttpClient._
import fs2.nakadi.model.{EnrichmentStrategy, NakadiConfig, PartitionStrategy}
import org.http4s.dsl.io.GET
import org.http4s.{Request, Status, Uri}

class RegistryInterpreter[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable]) extends Registries[F] {

  def enrichmentStrategies(implicit config: NakadiConfig[F]): F[List[EnrichmentStrategy]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "registry" / "enrichment-strategies"
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[List[EnrichmentStrategy]](request) {
                   case Status.Successful(l) => l.as[List[EnrichmentStrategy]]
                   case r                    => throwServerError(r)
                 }
    } yield response
  }

  def partitionStrategies(implicit config: NakadiConfig[F]): F[List[PartitionStrategy]] = {
    val uri        = Uri.unsafeFromString(config.uri.toString) / "registry" / "partition-strategies"
    val req        = Request[F](GET, uri)
    val httpClient = config.httpClient.getOrElse(defaultClient[F])

    for {
      request <- addBaseHeaders(req, config)
      response <- httpClient.fetch[List[PartitionStrategy]](request) {
                   case Status.Successful(l) => l.as[List[PartitionStrategy]]
                   case r                    => throwServerError(r)
                 }
    } yield response
  }
}

object RegistryInterpreter {
  def apply[F[_]: Async: ContextShift](implicit ME: MonadError[F, Throwable]): RegistryInterpreter[F] =
    new RegistryInterpreter()
}
