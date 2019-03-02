package fs2.nakadi
import java.util.UUID

import fs2.nakadi.model.FlowId

package object dsl {
  def randomFlowId = FlowId(UUID.randomUUID().toString)
}
