package fs2.nakadi.model

import io.circe.{Decoder, Encoder}
import io.circe.syntax._

final case class PartitionCompactionKey(key: String) extends AnyVal

object PartitionCompactionKey {
  implicit val encoder: Encoder[PartitionCompactionKey] =
    Encoder.instance[PartitionCompactionKey](_.key.asJson)
  implicit val decoder: Decoder[PartitionCompactionKey] =
    Decoder[String].map(PartitionCompactionKey.apply)

  def random: PartitionCompactionKey = PartitionCompactionKey(java.util.UUID.randomUUID().toString)
}
