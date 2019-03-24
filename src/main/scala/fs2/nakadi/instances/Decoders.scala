package fs2.nakadi.instances
import java.net.URI
import java.util.UUID

import fs2.nakadi.error.{BatchItemResponse, Problem, PublishingStatus, Step}
import fs2.nakadi.model.Event.{Business, DataChange, Undefined}
import fs2.nakadi.model.EventTypeSchema.Type
import fs2.nakadi.model.EventTypeStats.EventTypeStatsPartition
import fs2.nakadi.model.EventTypeStats.EventTypeStatsPartition.{AssignmentType, State}
import fs2.nakadi.model._
import io.circe.Decoder.Result
import io.circe.derivation.{deriveDecoder, renaming}
import io.circe.{Decoder, DecodingFailure, HCursor}

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

trait Decoders {
  implicit def dataChangeDecoder[T](implicit decoder: Decoder[T]): Decoder[DataChange[T]] =
    deriveDecoder(renaming.snakeCase)
  implicit def businessDecoder[T](implicit decoder: Decoder[T]): Decoder[Business[T]] =
    deriveDecoder(renaming.snakeCase)
  implicit def undefinedDecoder[T](implicit decoder: Decoder[T]): Decoder[Undefined[T]] =
    deriveDecoder(renaming.snakeCase)
  implicit def eventDecoder[T](implicit decoder: Decoder[T]): Decoder[Event[T]] =
    Decoder.instance[Event[T]](
      c => {
        val dataOpR   = c.downField("data_op").as[Option[String]]
        val metadataR = c.downField("metadata").as[Option[Metadata]]

        (for {
          dataOp   <- dataOpR
          metadata <- metadataR
        } yield {
          (dataOp, metadata) match {
            case (Some(_), Some(_)) =>
              c.as[Event.DataChange[T]]: Result[Event[T]]
            case (None, Some(_)) =>
              c.as[Event.Business[T]]: Result[Event[T]]
            case _ =>
              c.as[Event.Undefined[T]]: Result[Event[T]]
          }
        }).joinRight
      }
    )

  implicit lazy val dataOpDecoder: Decoder[DataOperation] = enumeratum.Circe.decoder(DataOperation)
  implicit lazy val metadataDecoder: Decoder[Metadata]    = deriveDecoder(renaming.snakeCase)
  implicit lazy val eventIdDecoder: Decoder[EventId]      = Decoder[String].map(EventId.apply)
  implicit lazy val partitionCompactionKeyDecoder: Decoder[PartitionCompactionKey] =
    Decoder[String].map(PartitionCompactionKey.apply)
  implicit lazy val spanCtxDecoder: Decoder[SpanCtx]             = Decoder[Map[String, String]].map(SpanCtx.apply)
  implicit lazy val writeScopeDecoder: Decoder[WriteScope]       = Decoder[String].map(WriteScope.apply)
  implicit lazy val readScopeDecoder: Decoder[ReadScope]         = Decoder[String].map(ReadScope.apply)
  implicit lazy val eventTypeNameDecoder: Decoder[EventTypeName] = Decoder[String].map(EventTypeName.apply)
  implicit lazy val audienceDecoder: Decoder[Audience]           = enumeratum.Circe.decoder(Audience)
  implicit lazy val categoryDecoder: Decoder[Category]           = enumeratum.Circe.decoder(Category)
  implicit lazy val enrichmentStrategyDecoder: Decoder[EnrichmentStrategy] =
    enumeratum.Circe.decoder(EnrichmentStrategy)
  implicit lazy val partitionStrategyDecoder: Decoder[PartitionStrategy]           = enumeratum.Circe.decoder(PartitionStrategy)
  implicit lazy val cleanupPolicyDecoder: Decoder[CleanupPolicy]                   = enumeratum.Circe.decoder(CleanupPolicy)
  implicit lazy val compatibilityModeDecoder: Decoder[CompatibilityMode]           = enumeratum.Circe.decoder(CompatibilityMode)
  implicit lazy val typeDecoder: Decoder[Type]                                     = enumeratum.Circe.decoder(Type)
  implicit lazy val eventTypeSchemaDecoder: Decoder[EventTypeSchema]               = deriveDecoder(renaming.snakeCase)
  implicit lazy val eventTypeStatisticsDecoder: Decoder[EventTypeStatistics]       = deriveDecoder(renaming.snakeCase)
  implicit lazy val authorizationAttributeDecoder: Decoder[AuthorizationAttribute] = deriveDecoder(renaming.snakeCase)
  implicit lazy val eventTypeAuthorizationDecoder: Decoder[EventTypeAuthorization] = deriveDecoder(renaming.snakeCase)
  implicit lazy val eventTypeOptionsDecoder: Decoder[EventTypeOptions]             = deriveDecoder(renaming.snakeCase)
  implicit lazy val eventTypeDecoder: Decoder[EventType]                           = deriveDecoder(renaming.snakeCase)
  implicit lazy val flowIdDecoder: Decoder[FlowId]                                 = Decoder[String].map(FlowId.apply)
  implicit lazy val streamIdDecoder: Decoder[StreamId]                             = Decoder[String].map(StreamId.apply)
  implicit lazy val subscriptionIdDecoder: Decoder[SubscriptionId]                 = Decoder[UUID].map(SubscriptionId.apply)
  implicit lazy val subscriptionAuthorizationDecoder: Decoder[SubscriptionAuthorization] = deriveDecoder(
    renaming.snakeCase)
  implicit lazy val subscriptionDecoder: Decoder[Subscription] = deriveDecoder(renaming.snakeCase)
  implicit lazy val subscriptionQueryDecoder: Decoder[SubscriptionQuery] =
    Decoder.forProduct2(
      "_links",
      "items"
    )(SubscriptionQuery.apply)
  implicit lazy val paginationLinkDecoder: Decoder[PaginationLink]               = Decoder.forProduct1("href")(PaginationLink.apply)
  implicit lazy val paginationLinksDecoder: Decoder[PaginationLinks]             = deriveDecoder(renaming.snakeCase)
  implicit lazy val subscriptionCursorDecoder: Decoder[SubscriptionCursor]       = deriveDecoder(renaming.snakeCase)
  implicit lazy val subscriptionEventInfoDecoder: Decoder[SubscriptionEventInfo] = deriveDecoder(renaming.snakeCase)
  implicit def subscriptionEventDataDecoder[T: Decoder]: Decoder[SubscriptionEventData[T]] =
    deriveDecoder(renaming.snakeCase)
  implicit def subscriptionEventDecoder[T: Decoder]: Decoder[SubscriptionEvent[T]] = deriveDecoder(renaming.snakeCase)
  implicit lazy val subscriptionStatsDecoder: Decoder[SubscriptionStats]           = deriveDecoder(renaming.snakeCase)
  implicit lazy val commitCursorItemResponseDecoder: Decoder[CommitCursorItemResponse] = deriveDecoder(
    renaming.snakeCase)
  implicit lazy val commitCursorResponseDecoder: Decoder[CommitCursorResponse] = deriveDecoder(renaming.snakeCase)
  implicit lazy val cursorDecoder: Decoder[Cursor]                             = deriveDecoder(renaming.snakeCase)
  implicit lazy val partitionDecoder: Decoder[Partition]                       = Decoder[String].map(Partition.apply)
  implicit lazy val cursorTokenDecoder: Decoder[CursorToken]                   = Decoder[UUID].map(CursorToken.apply)
  implicit lazy val stateDecoder: Decoder[State]                               = enumeratum.Circe.decoder(State)
  implicit lazy val assignmentTypeDecoder: Decoder[AssignmentType]             = enumeratum.Circe.decoder(AssignmentType)
  implicit lazy val eventTypeStatsPartitionDecoder: Decoder[EventTypeStatsPartition] = {
    implicit lazy val finiteDurationSecondsDecoder: Decoder[FiniteDuration] =
      Decoder[Long].map(seconds => FiniteDuration(seconds, scala.concurrent.duration.SECONDS))

    deriveDecoder(renaming.snakeCase)
  }

  implicit lazy val eventTypeStatsDecoder: Decoder[EventTypeStats] = deriveDecoder(renaming.snakeCase)
  implicit lazy val uriDecoder: Decoder[URI] = (c: HCursor) => {
    c.as[String].flatMap { value =>
      try {
        Right(new URI(value))
      } catch {
        case NonFatal(_) => Left(DecodingFailure("Invalid Uri", c.history))
      }
    }
  }

  implicit lazy val batchItemResponseDecoder: Decoder[BatchItemResponse] =
    Decoder.forProduct4(
      "eid",
      "publishing_status",
      "step",
      "detail"
    )(BatchItemResponse.apply)

  implicit lazy val publishingStatusDecoder: Decoder[PublishingStatus] = enumeratum.Circe.decoder(PublishingStatus)
  implicit lazy val stepDecoder: Decoder[Step]                         = enumeratum.Circe.decoder(Step)

  implicit lazy val problemDecoder: Decoder[Problem] = deriveDecoder(renaming.snakeCase)
}
