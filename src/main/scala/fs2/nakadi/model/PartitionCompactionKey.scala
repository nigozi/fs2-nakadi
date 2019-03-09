package fs2.nakadi.model

import java.util.UUID

import io.circe.syntax._
import io.circe.{Decoder, Encoder}

final case class PartitionCompactionKey(key: String) extends AnyVal

object PartitionCompactionKey {
  implicit val encoder: Encoder[PartitionCompactionKey] = Encoder.instance[PartitionCompactionKey](_.key.asJson)
  implicit val decoder: Decoder[PartitionCompactionKey] = Decoder[String].map(PartitionCompactionKey.apply)

  def random: PartitionCompactionKey = PartitionCompactionKey(UUID.randomUUID().toString)
}
