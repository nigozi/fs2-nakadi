package fs2.nakadi
import java.util.concurrent.Executors

import cats.effect.{Async, ContextShift}
import org.http4s.client.{Client, JavaNetClientBuilder}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

package object dsl {
  private[dsl] def httpClient[F[_]: Async](implicit cs: ContextShift[F]): Client[F] = {
    val blockingEC: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

    JavaNetClientBuilder[F](blockingEC).create
  }
}
