package fs2.nakadi.model

import io.circe.syntax._
import io.circe.{Decoder, Encoder}

case class FlowId(id: String) extends AnyVal

object FlowId {
  implicit val encoder: Encoder[FlowId] = Encoder.instance[FlowId](_.id.asJson)
  implicit val decoder: Decoder[FlowId] = Decoder[String].map(FlowId.apply)
}
