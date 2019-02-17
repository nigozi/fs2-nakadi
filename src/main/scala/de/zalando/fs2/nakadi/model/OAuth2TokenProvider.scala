package de.zalando.fs2.nakadi.model
import cats.Monad

case class OAuth2Token(token: String) extends AnyVal

case class OAuth2TokenProvider[F[_]: Monad](provider: () => F[OAuth2Token])
