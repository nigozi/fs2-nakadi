package fs2.nakadi.model

import io.circe.derivation._
import io.circe.{Decoder, Encoder}

final case class PartitionCompactionKey(key: String) extends AnyVal

object PartitionCompactionKey {
  implicit val encoder: Encoder[PartitionCompactionKey] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[PartitionCompactionKey] = deriveDecoder(renaming.snakeCase)

  def random: PartitionCompactionKey = PartitionCompactionKey(java.util.UUID.randomUUID().toString)
}
