package fs2.nakadi.model

import io.circe.syntax._
import io.circe.{Decoder, Encoder}

final case class EventId(id: String) extends AnyVal

object EventId {
  implicit val encoder: Encoder[EventId] =
    Encoder.instance[EventId](_.id.asJson)
  implicit val decoder: Decoder[EventId] =
    Decoder[String].map(EventId.apply)

  def random: EventId = EventId(java.util.UUID.randomUUID().toString)
}
