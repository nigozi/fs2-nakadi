package fs2.nakadi.api

import java.net.URI

import cats.effect.IO
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s.{Header, Headers, Request, Status, Uri}
import org.http4s.Method.GET

import fs2.nakadi.error._
import fs2.nakadi.model._

trait RegistryAlg[F[_]] {
  def enrichmentStrategies(implicit flowId: FlowId): F[List[String]]

  def partitionStrategies(implicit flowId: FlowId): F[List[String]]
}

class Registries(uri: URI, oAuth2TokenProvider: Option[OAuth2TokenProvider]) extends RegistryAlg[IO] {
  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[Registries])

  val baseUri: Uri = Uri.unsafeFromString(uri.toString)

  override def enrichmentStrategies(implicit flowId: FlowId): IO[List[String]] = {
    val uri         = baseUri / "registry" / "enrichment-strategies"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[List[String]](request) {
                   case Status.Successful(l) => l.as[List[String]]
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }

  override def partitionStrategies(implicit flowId: FlowId): IO[List[String]] = {
    val uri         = baseUri / "registry" / "partition-strategies"
    val baseHeaders = List(Header("X-Flow-ID", flowId.id))

    for {
      headers <- addAuth(baseHeaders, oAuth2TokenProvider)
      request = Request[IO](GET, uri, headers = Headers(headers))
      _       = logger.debug(request.toString)
      response <- httpClient.fetch[List[String]](request) {
                   case Status.Successful(l) => l.as[List[String]]
                   case r                    => r.as[String].flatMap(e => IO.raiseError(GeneralError(e)))
                 }
    } yield response
  }
}
