package fs2.nakadi.model

import io.circe.derivation._
import io.circe.{Decoder, Encoder}

final case class EventId(id: String) extends AnyVal

object EventId {
  implicit val encoder: Encoder[EventId] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventId] = deriveDecoder(renaming.snakeCase)

  def random: EventId = EventId(java.util.UUID.randomUUID().toString)
}
