package de.zalando.fs2.nakadi.impl
import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import de.zalando.fs2.nakadi.model._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.Client

class Registry[F[_]: Monad: Sync](client: Client[F]) {

  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[Registry[F]])

  def enrichmentStrategies(implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, List[String]]] =
    Kleisli { config =>
      val uri         = config.baseUri / "registry" / "enrichment-strategies"
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, config.oAuth2TokenProvider)
        request = Request[F](GET, uri, headers = Headers(headers))
        _       = logger.debug(request.toString)
        response <- httpClient(config, client).fetch[Either[String, List[String]]](request) {
                     case Status.Successful(r) => r.attemptAs[List[String]].leftMap(_.message).value
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }

  def partitionStrategies(
      implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, List[PartitionStrategy]]] =
    Kleisli { config =>
      val uri         = config.baseUri / "registry" / "partition-strategies"
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, config.oAuth2TokenProvider)
        request = Request[F](GET, uri, headers = Headers(headers))
        _       = logger.debug(request.toString)
        response <- httpClient(config, client).fetch[Either[String, List[PartitionStrategy]]](request) {
                     case Status.Successful(r) => r.attemptAs[List[PartitionStrategy]].leftMap(_.message).value
                     case r                    => r.as[String].map(_.asLeft)
                   }
      } yield response
    }
}
