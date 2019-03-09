package fs2.nakadi.model

import io.circe.syntax._
import io.circe.{Decoder, Encoder}

final case class Partition(id: String) extends AnyVal

object Partition {
  implicit val encoder: Encoder[Partition] = Encoder.instance[Partition](_.id.asJson)
  implicit val decoder: Decoder[Partition] = Decoder[String].map(Partition.apply)
}
