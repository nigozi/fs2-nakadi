package fs2.nakadi.model

import io.circe.{Decoder, Encoder}
import io.circe.syntax._

final case class EventId(id: String) extends AnyVal

object EventId {
  implicit val eventIdEncoder: Encoder[EventId] =
    Encoder.instance[EventId](_.id.asJson)
  implicit val eventIdDecoder: Decoder[EventId] =
    Decoder[String].map(EventId.apply)

  def random: EventId = EventId(java.util.UUID.randomUUID().toString)
}
