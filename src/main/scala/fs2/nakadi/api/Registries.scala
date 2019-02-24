package fs2.nakadi.api

import cats.effect.IO
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.http4s.Method.GET

import fs2.nakadi.error._
import fs2.nakadi.model._

trait RegistryAlg[F[_]] {
  def enrichmentStrategies(implicit flowId: FlowId = randomFlowId()): F[List[EnrichmentStrategy]]

  def partitionStrategies(implicit flowId: FlowId = randomFlowId()): F[List[PartitionStrategy]]
}

class Registries(config: NakadiConfig) extends RegistryAlg[IO] with Implicits {
  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[Registries])

  private val baseUri       = Uri.unsafeFromString(config.uri.toString)
  private val tokenProvider = config.tokenProvider
  private val httpClient    = config.httpClient.getOrElse(defaultClient)

  override def enrichmentStrategies(implicit flowId: FlowId): IO[List[EnrichmentStrategy]] = {
    val uri         = baseUri / "registry" / "enrichment-strategies"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[List[EnrichmentStrategy]](request) {
                     case Status.Successful(l) => l.as[List[EnrichmentStrategy]]
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }

  override def partitionStrategies(implicit flowId: FlowId): IO[List[PartitionStrategy]] = {
    val uri         = baseUri / "registry" / "partition-strategies"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, tokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient
                   .fetch[List[PartitionStrategy]](request) {
                     case Status.Successful(l) => l.as[List[PartitionStrategy]]
                     case r                    => r.as[String].flatMap(e => IO.raiseError(ServerError(r.status.code, e)))
                   }
    } yield response
  }
}
