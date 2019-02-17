package de.zalando.fs2.nakadi
import java.util
import java.util.UUID

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.CanLog
import de.zalando.fs2.nakadi.model.{FlowId, NakadiConfig, OAuth2Token, OAuth2TokenProvider}
import io.circe.{Decoder, Encoder}
import org.http4s.Credentials.Token
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, EntityEncoder, Header}
import org.slf4j.MDC

package object impl {
  private[impl] implicit final val canLogFlowId: CanLog[FlowId] = new CanLog[FlowId] {
    override def logMessage(originalMsg: String, flowId: FlowId): String = {
      MDC.put("flow_id", flowId.id)
      originalMsg
    }

    override def afterLog(flowId: FlowId): Unit = {
      MDC.remove("flow_id")
    }
  }

  private[impl] def toHeader(oAuth2Token: OAuth2Token): Header =
    Authorization(Token(CaseInsensitiveString("Bearer"), oAuth2Token.token))

  private[impl] def addAuth[F[_]: Monad](baseHeaders: List[Header],
                                         oAuth2TokenProvider: Option[OAuth2TokenProvider[F]]): F[List[Header]] =
    oAuth2TokenProvider match {
      case Some(tp) => tp.provider.apply().map(toHeader).map(_ :: baseHeaders)
      case None     => Monad[F].pure(baseHeaders)
    }

  private[impl] def randomFlowId() = FlowId(UUID.randomUUID().toString)

  private[impl] def httpClient[F[_]](config: NakadiConfig[F], default: Client[F]): Client[F] =
    config.httpClient.getOrElse(default)

  private[impl] implicit def listDecoder[F[_]: Sync, T <: util.Collection[_]: Decoder](
      implicit ed: EntityDecoder[F, T]): EntityDecoder[F, List[T]] =
    jsonOf[F, List[T]]

  private[impl] implicit def entityEncoder[F[_]: Sync, T](implicit e: Encoder[T]): EntityEncoder[F, T] =
    jsonEncoderOf[F, T]

  private[impl] implicit def entityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]
}
