package fs2.nakadi.model
import cats.Monad

case class Token(value: String) extends AnyVal

case class TokenProvider[F[_]: Monad](provider: () => F[Token])
