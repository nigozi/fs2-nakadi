package fs2.nakadi.model

import cats.effect.IO

case class Token(value: String) extends AnyVal

case class TokenProvider(provider: () => IO[Token])
