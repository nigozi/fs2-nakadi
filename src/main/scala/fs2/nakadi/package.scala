package fs2
import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{Async, ContextShift}
import com.typesafe.scalalogging.CanLog
import fs2.nakadi.instances.{Decoders, Encoders}
import fs2.nakadi.model.FlowId
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

package object nakadi {
  private[nakadi] def httpClient[F[_]: Async](implicit cs: ContextShift[F]): Client[F] = {
    val blockingEC: ExecutionContextExecutorService =
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))

    JavaNetClientBuilder[F](blockingEC).create
  }

  private[nakadi] def randomFlowId() = FlowId(UUID.randomUUID().toString)

  private[nakadi] implicit final val canLogFlowId: CanLog[FlowId] = new CanLog[FlowId] {
    override def logMessage(originalMsg: String, flowId: FlowId): String = {
      MDC.put("flow_id", flowId.id)
      originalMsg
    }

    override def afterLog(flowId: FlowId): Unit = {
      MDC.remove("flow_id")
    }
  }

  object implicits extends Encoders with Decoders
}
