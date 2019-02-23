package fs2.nakadi.model

import io.circe.{Decoder, Encoder}
import io.circe.syntax._

final case class SpanCtx(ctx: Map[String, String]) extends AnyVal

object SpanCtx {
  implicit val encoder: Encoder[SpanCtx] =
    Encoder.instance[SpanCtx](_.ctx.asJson)
  implicit val decoder: Decoder[SpanCtx] =
    Decoder[Map[String, String]].map(SpanCtx.apply)
}
