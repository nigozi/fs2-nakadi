package fs2.nakadi
import java.util.UUID

import cats.effect.{Async, Sync}
import cats.instances.option._
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.{Monad, MonadError}
import fs2.nakadi.error.{GeneralError, ServerError}
import fs2.nakadi.model.{NakadiConfig, StreamId, Token}
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, Printer}
import org.http4s
import org.http4s.circe._
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Header, Request, Response}

package object interpreters {
  private[interpreters] val XNakadiStreamId = "X-Nakadi-StreamId"
  private[interpreters] val XFlowId         = "X-Flow-ID"

  private[interpreters] def addHeaders[F[_]: Async](req: Request[F],
                                                    config: NakadiConfig[F],
                                                    headers: List[Header] = Nil): F[Request[F]] = {
    val baseHeaders = Header(XFlowId, UUID.randomUUID().toString) :: headers
    val allHeaders = config.tokenProvider.traverse(_.provider.apply().map(authHeader)).map {
      case Some(h) => h :: baseHeaders
      case None    => baseHeaders
    }

    allHeaders.map(h => req.putHeaders(h: _*))
  }

  private[interpreters] def authHeader(token: Token): Header =
    Authorization(http4s.Credentials.Token(CaseInsensitiveString("Bearer"), token.value))

  private[interpreters] def encode[T: Encoder](entity: T): Json =
    parse(
      Printer.noSpaces
        .copy(dropNullValues = true)
        .pretty(entity.asJson)
    ).valueOr(e => sys.error(s"failed to encode the entity: ${e.message}"))

  private[interpreters] def throwServerError[F[_], T](
      r: Response[F])(implicit M: Monad[F], ME: MonadError[F, Throwable], D: EntityDecoder[F, String]): F[T] =
    r.as[String]
      .map(e => ServerError(r.status.code, Some(e)))
      .handleError(_ => ServerError(r.status.code, None))
      .flatMap(e => ME.raiseError(e))

  private[interpreters] def streamId[F[_]](r: Response[F]): StreamId =
    r.headers
      .get(CaseInsensitiveString(XNakadiStreamId))
      .map(h => StreamId(h.value)) match {
      case Some(sid) => sid
      case None      => throw GeneralError(s"$XNakadiStreamId header is missing")
    }

  private[interpreters] implicit def entityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]
}
