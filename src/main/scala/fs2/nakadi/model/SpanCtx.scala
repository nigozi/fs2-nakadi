package fs2.nakadi.model

import io.circe.derivation._
import io.circe.{Decoder, Encoder}

final case class SpanCtx(ctx: Map[String, String]) extends AnyVal

object SpanCtx {
  implicit val encoder: Encoder[SpanCtx] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[SpanCtx] = deriveDecoder(renaming.snakeCase)
}
