package fs2.nakadi
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

trait TestResources {
  implicit def entityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]
  implicit def entityEncoder[F[_]: Sync, T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf[F, T]
}
