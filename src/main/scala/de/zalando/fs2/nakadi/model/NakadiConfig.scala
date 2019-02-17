package de.zalando.fs2.nakadi.model
import java.net.URI

import cats.Monad
import org.http4s.Uri
import org.http4s.client.Client

case class OAuth2Token(token: String) extends AnyVal

case class OAuth2TokenProvider[F[_]: Monad](provider: () => F[OAuth2Token])

case class NakadiConfig[F[_]](uri: URI,
                              oAuth2TokenProvider: Option[OAuth2TokenProvider[F]] = None,
                              httpClient: Option[Client[F]] = None) {
  val baseUri: Uri = Uri.unsafeFromString(uri.toString)
}
