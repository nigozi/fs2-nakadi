package fs2.nakadi.model

import io.circe.derivation._
import io.circe.{Decoder, Encoder}

final case class Partition(id: String) extends AnyVal

object Partition {
  implicit val encoder: Encoder[Partition] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[Partition] = deriveDecoder(renaming.snakeCase)
}
