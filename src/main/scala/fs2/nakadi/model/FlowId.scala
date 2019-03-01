package fs2.nakadi.model

import io.circe.derivation._
import io.circe.{Decoder, Encoder}

case class FlowId(id: String) extends AnyVal

object FlowId {
  implicit val encoder: Encoder[FlowId] = deriveEncoder[FlowId]
  implicit val decoder: Decoder[FlowId] = deriveDecoder[FlowId]
}
