package fs2.nakadi.interpreters
import cats.effect.{Async, Sync}
import cats.instances.option._
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.{Monad, MonadError}
import fs2.nakadi.error._
import fs2.nakadi.implicits._
import fs2.nakadi.model.{FlowId, NakadiConfig, Token}
import fs2.nakadi.randomFlowId
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.http4s
import org.http4s.circe.jsonOf
import org.http4s.headers.Authorization
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Header, Request, Response}

trait Interpreter {
  protected val XNakadiStreamId = "X-Nakadi-StreamId"
  protected val XFlowId         = "X-Flow-ID"

  implicit def entityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]

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

  def unsuccessfulOperation[F[_]: Sync, T](
      r: Response[F])(implicit M: Monad[F], ME: MonadError[F, Throwable], D: EntityDecoder[F, String]): F[T] = {
    val json = r.as[Json].handleErrorWith(e => ME.raiseError(UnknownError(e.getMessage)))

    json.flatMap { json =>
      json.as[Problem] match {
        case Left(_) =>
          json.as[BasicServerError] match {
            case Left(error) =>
              ME.raiseError(UnknownError(error.message))
            case Right(basicServerError) =>
              ME.raiseError(OtherError(basicServerError))
          }
        case Right(problem) => ME.raiseError(GeneralError(problem))
      }
    }
  }
}
