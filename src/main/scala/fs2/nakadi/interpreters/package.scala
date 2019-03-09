package fs2.nakadi
import cats.effect.Sync
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Monad, MonadError}
import fs2.nakadi.error.ServerError
import fs2.nakadi.model.StreamId
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, Printer}
import org.http4s.circe._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Response}

package object interpreters {
  private[interpreters] val xNakadiStreamIdHeader = "X-Nakadi-StreamId"

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
      .get(CaseInsensitiveString(xNakadiStreamIdHeader))
      .map(h => StreamId(h.value)) match {
      case Some(sid) => sid
      case None      => sys.error(s"$xNakadiStreamIdHeader header is missing")
    }

  implicit def entityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]
}
