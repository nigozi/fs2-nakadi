package fs2.nakadi.model

import java.net.URI

import io.circe.{Decoder, Encoder}

final case class PaginationLink(href: URI) extends AnyVal

object PaginationLink {
  implicit val encoder: Encoder[PaginationLink] = Encoder.forProduct1("href")(_.href)
  implicit val decoder: Decoder[PaginationLink] = Decoder.forProduct1("href")(PaginationLink.apply)
}
