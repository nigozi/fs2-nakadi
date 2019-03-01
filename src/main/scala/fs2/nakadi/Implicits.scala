package fs2.nakadi

import cats.effect.IO
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}

trait Implicits {
  implicit def listDecoder[T: Decoder](implicit ed: EntityDecoder[IO, T]): EntityDecoder[IO, List[T]] =
    jsonOf[IO, List[T]]

  implicit def listEncoder[T: Encoder](implicit ed: EntityEncoder[IO, T]): EntityEncoder[IO, List[T]] =
    jsonEncoderOf[IO, List[T]]
}

object Implicits extends Implicits
