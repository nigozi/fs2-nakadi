package fs2.nakadi.model

import io.circe.derivation._
import io.circe.{Decoder, Encoder}

final case class PaginationLinks(prev: Option[PaginationLink], next: Option[PaginationLink])

object PaginationLinks {
  implicit val encoder: Encoder[PaginationLinks] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[PaginationLinks] = deriveDecoder(renaming.snakeCase)
}
