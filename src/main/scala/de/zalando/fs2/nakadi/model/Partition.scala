package de.zalando.fs2.nakadi.model

import io.circe.{Decoder, Encoder}
import io.circe.syntax._

final case class Partition(id: String) extends AnyVal

object Partition {
  implicit val partitionEncoder: Encoder[Partition] = Encoder.instance[Partition](_.id.asJson)
  implicit val partitionDecoder: Decoder[Partition] = Decoder[String].map(Partition.apply)
}
