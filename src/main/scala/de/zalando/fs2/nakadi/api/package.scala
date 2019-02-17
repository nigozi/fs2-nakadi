package de.zalando.fs2.nakadi
import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import de.zalando.fs2.nakadi.model.FlowId
import org.http4s.client.{Client, JavaNetClientBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

package object api {
  private[api] implicit val cs: ContextShift[IO] = IO.contextShift(global)

  private[api] val blockingEC: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

  private[api] val httpClient: Client[IO] = JavaNetClientBuilder(blockingEC).create[IO]

  private[api] def randomFlowId() = FlowId(UUID.randomUUID().toString)
}
