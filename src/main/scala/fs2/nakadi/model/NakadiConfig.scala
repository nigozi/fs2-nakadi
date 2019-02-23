package fs2.nakadi.model
import java.net.URI

import cats.effect.IO
import org.http4s.client.Client

case class NakadiConfig(uri: URI, tokenProvider: Option[TokenProvider] = None, httpClient: Option[Client[IO]] = None)
