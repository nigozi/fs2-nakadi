package fs2
import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{Async, ContextShift}
import fs2.nakadi.instances.{Decoders, Encoders}
import fs2.nakadi.model.FlowId
import org.http4s.client.{Client, JavaNetClientBuilder}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

package object nakadi {
  private[nakadi] def httpClient[F[_]: Async](implicit cs: ContextShift[F]): Client[F] = {
    val blockingEC: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

    JavaNetClientBuilder[F](blockingEC).create
  }

  private[nakadi] def randomFlowId() = FlowId(UUID.randomUUID().toString)

  object implicits extends Encoders with Decoders
}
