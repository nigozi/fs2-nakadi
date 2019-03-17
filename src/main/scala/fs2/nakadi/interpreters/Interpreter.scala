package fs2.nakadi.interpreters
import cats.effect.{Async, Sync}
import cats.instances.option._
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.{Monad, MonadError}
import fs2.nakadi.error.{GeneralError, ServerError}
import fs2.nakadi.model.{FlowId, NakadiConfig, StreamId, Token}
import fs2.nakadi.randomFlowId
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, Printer}
import org.http4s
import org.http4s.circe._
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Header, Request, Response}

trait Interpreter {
  val XNakadiStreamId = "X-Nakadi-StreamId"
  val XFlowId         = "X-Flow-ID"

  def addHeaders[F[_]: Async](req: Request[F], headers: List[Header] = Nil)(
      implicit config: NakadiConfig[F],
      flowId: FlowId = randomFlowId()): F[Request[F]] = {
    val baseHeaders = Header(XFlowId, flowId.id) :: headers
    val allHeaders = config.tokenProvider.traverse(_.provider.apply().map(authHeader)).map {
      case Some(h) => h :: baseHeaders
      case None    => baseHeaders
    }

    allHeaders.map(h => req.putHeaders(h: _*))
  }

  def authHeader(token: Token): Header =
    Authorization(http4s.Credentials.Token(CaseInsensitiveString("Bearer"), token.value))

  def encode[T: Encoder](entity: T): Json =
    parse(
      Printer.noSpaces
        .copy(dropNullValues = true)
        .pretty(entity.asJson)
    ).valueOr(e => sys.error(s"failed to encode the entity: ${e.message}"))

  def throwServerError[F[_], T](
      r: Response[F])(implicit M: Monad[F], ME: MonadError[F, Throwable], D: EntityDecoder[F, String]): F[T] =
    r.as[String]
      .map(e => ServerError(r.status.code, Some(e)))
      .handleError(_ => ServerError(r.status.code, None))
      .flatMap(e => ME.raiseError(e))

  def streamId[F[_]](r: Response[F]): StreamId =
    r.headers
      .get(CaseInsensitiveString(XNakadiStreamId))
      .map(h => StreamId(h.value)) match {
      case Some(sid) => sid
      case None      => throw GeneralError(s"$XNakadiStreamId header is missing")
    }

  implicit def entityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]
}
