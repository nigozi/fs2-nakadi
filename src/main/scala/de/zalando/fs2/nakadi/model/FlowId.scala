package de.zalando.fs2.nakadi.model

import io.circe.{Decoder, Encoder}
import io.circe.derivation._

case class FlowId(id: String) extends AnyVal

object FlowId {
  implicit val decoder: Decoder[FlowId] = deriveDecoder[FlowId]
  implicit val encoder: Encoder[FlowId] = deriveEncoder[FlowId]
}
