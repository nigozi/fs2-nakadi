package fs2.nakadi

import cats.effect.{ContextShift, IO, Sync}
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

import scala.concurrent.ExecutionContext.Implicits.global

trait Implicits {
  implicit def listDecoder[F[_]: Sync, T: Decoder](implicit ed: EntityDecoder[F, T]): EntityDecoder[F, List[T]] =
    jsonOf[F, List[T]]

  implicit def listEncoder[F[_]: Sync, T: Encoder](implicit ed: EntityEncoder[F, T]): EntityEncoder[F, List[T]] =
    jsonEncoderOf[F, List[T]]

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
}

object Implicits extends Implicits
