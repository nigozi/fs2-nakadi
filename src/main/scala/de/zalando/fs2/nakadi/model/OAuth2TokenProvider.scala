package de.zalando.fs2.nakadi.model

import cats.effect.IO

case class OAuth2Token(token: String) extends AnyVal

case class OAuth2TokenProvider(provider: () => IO[OAuth2Token])
