package fs2.nakadi.model
import java.net.URI

import org.http4s.client.Client

case class NakadiConfig[F[_]](uri: URI,
                              tokenProvider: Option[TokenProvider[F]] = None,
                              httpClient: Option[Client[F]] = None)
