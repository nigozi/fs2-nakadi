package fs2.nakadi.error

import enumeratum.{Enum, EnumEntry}
import fs2.nakadi.model.EventId

import scala.collection.immutable

final case class BatchItemResponse(eid: Option[EventId],
                                   publishingStatus: PublishingStatus,
                                   step: Option[Step],
                                   detail: Option[String])

sealed abstract class PublishingStatus(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object PublishingStatus extends Enum[PublishingStatus] {
  val values: immutable.IndexedSeq[PublishingStatus] = findValues
  case object Submitted extends PublishingStatus("submitted")
  case object Failed    extends PublishingStatus("failed")
  case object Aborted   extends PublishingStatus("aborted")
}

sealed abstract class Step(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object Step extends Enum[Step] {
  val values: immutable.IndexedSeq[Step] = findValues
  case object None         extends Step("none")
  case object Validating   extends Step("validating")
  case object Partitioning extends Step("partitioning")
  case object Enriching    extends Step("enriching")
  case object Publishing   extends Step("publishing")
}

final case class EventValidation(batchItemResponse: List[BatchItemResponse]) extends Exception {
  override def getMessage: String =
    s"Error publishing events, errors are ${batchItemResponse.mkString("\n")}"
}
