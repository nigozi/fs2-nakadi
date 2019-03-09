package fs2.nakadi.model

import io.circe.syntax._
import io.circe.{Decoder, Encoder}

final case class StreamId(id: String) extends AnyVal

object StreamId {
  implicit val encoder: Encoder[StreamId] = Encoder.instance[StreamId](_.id.asJson)
  implicit val decoder: Decoder[StreamId] = Decoder[String].map(StreamId.apply)
}
