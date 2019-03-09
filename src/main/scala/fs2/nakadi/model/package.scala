package fs2.nakadi
import java.net.URI

import io.circe.syntax._
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}

import scala.util.control.NonFatal

package object model {
  private[model] implicit val uriEncoder: Encoder[URI] =
    Encoder.instance[URI](_.toString.asJson)

  private[model] implicit val uriDecoder: Decoder[URI] = (c: HCursor) => {
    c.as[String].flatMap { value =>
      try {
        Right(new URI(value))
      } catch {
        case NonFatal(_) => Left(DecodingFailure("Invalid Uri", c.history))
      }
    }
  }
}
