package fs2.nakadi.error

import scala.collection.immutable

import fs2.nakadi.model.EventId
import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

final case class BatchItemResponse(eid: Option[EventId],
                                   publishingStatus: PublishingStatus,
                                   step: Option[Step],
                                   detail: Option[String])

object BatchItemResponse {
  implicit val batchItemResponseEncoder: Encoder[BatchItemResponse] =
    Encoder.forProduct4(
      "eid",
      "publishing_status",
      "step",
      "detail"
    )(x => BatchItemResponse.unapply(x).get)

  implicit val batchItemResponseDecoder: Decoder[BatchItemResponse] =
    Decoder.forProduct4(
      "eid",
      "publishing_status",
      "step",
      "detail"
    )(BatchItemResponse.apply)
}

sealed abstract class PublishingStatus(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object PublishingStatus extends Enum[PublishingStatus] {
  val values: immutable.IndexedSeq[PublishingStatus] = findValues
  case object Submitted extends PublishingStatus("submitted")
  case object Failed    extends PublishingStatus("failed")
  case object Aborted   extends PublishingStatus("aborted")

  implicit val eventsErrorsPublishingStatusEncoder: Encoder[PublishingStatus] =
    enumeratum.Circe.encoder(PublishingStatus)
  implicit val eventsErrorsPublishingStatusDecoder: Decoder[PublishingStatus] =
    enumeratum.Circe.decoder(PublishingStatus)
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

  implicit val eventsErrorsStepEncoder: Encoder[Step] =
    enumeratum.Circe.encoder(Step)
  implicit val eventsErrorsStepDecoder: Decoder[Step] =
    enumeratum.Circe.decoder(Step)
}

final case class EventValidation(batchItemResponse: List[BatchItemResponse]) extends Exception {
  override def getMessage: String =
    s"Error publishing events, errors are ${batchItemResponse.mkString("\n")}"
}
