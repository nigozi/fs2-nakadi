package fs2.nakadi.model
import java.net.URI

import scala.concurrent.duration.{FiniteDuration, _}

case class NakadiConfig[F[_]](uri: URI,
                              tokenProvider: Option[TokenProvider[F]] = None,
                              noEmptySlotsCursorResetRetryDelay: FiniteDuration = 30.seconds)
