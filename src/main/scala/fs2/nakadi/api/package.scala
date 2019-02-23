package fs2.nakadi

import java.util
import java.util.concurrent.Executors
import java.util.UUID

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.{ContextShift, IO}
import cats.syntax.applicative._
import com.typesafe.scalalogging.CanLog
import org.http4s.{EntityDecoder, EntityEncoder, Header}
import org.http4s
import org.http4s.circe._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.slf4j.MDC

import fs2.nakadi.model.{FlowId, Token, TokenProvider}
import io.circe.{Decoder, Encoder}

package object api {
  private[api] implicit val cs: ContextShift[IO] = IO.contextShift(global)

  private[api] val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

  private[api] val defaultClient: Client[IO] = JavaNetClientBuilder(blockingEC).create[IO]

  private[api] def randomFlowId() = FlowId(UUID.randomUUID().toString)

  private[api] implicit final val canLogFlowId: CanLog[FlowId] = new CanLog[FlowId] {
    override def logMessage(originalMsg: String, flowId: FlowId): String = {
      MDC.put("flow_id", flowId.id)
      originalMsg
    }

    override def afterLog(flowId: FlowId): Unit = {
      MDC.remove("flow_id")
    }
  }

  private[api] def toHeader(token: Token): Header =
    Authorization(http4s.Credentials.Token(CaseInsensitiveString("Bearer"), token.value))

  private[api] def addAuth(baseHeaders: List[Header], tokenProvider: Option[TokenProvider]): IO[List[Header]] =
    tokenProvider match {
      case Some(tp) => tp.provider.apply().map(toHeader).map(_ :: baseHeaders)
      case None     => baseHeaders.pure[IO]
    }

  private[api] implicit def listDecoder[T <: util.Collection[_]: Decoder](
      implicit ed: EntityDecoder[IO, T]): EntityDecoder[IO, List[T]] =
    jsonOf[IO, List[T]]

  private[api] implicit def entityEncoder[T](implicit e: Encoder[T]): EntityEncoder[IO, T] =
    jsonEncoderOf[IO, T]

  private[api] implicit def entityDecoder[T: Decoder]: EntityDecoder[IO, T] = jsonOf[IO, T]
}
