package fs2.nakadi.instances
import java.net.URI

import fs2.nakadi.error.{BatchItemResponse, Problem, PublishingStatus, Step}
import fs2.nakadi.model.Event.{Business, DataChange, Undefined}
import fs2.nakadi.model.EventTypeSchema.Type
import fs2.nakadi.model.EventTypeStats.EventTypeStatsPartition
import fs2.nakadi.model.EventTypeStats.EventTypeStatsPartition.{AssignmentType, State}
import fs2.nakadi.model._
import io.circe.{Decoder, Encoder}
import io.circe.derivation.{deriveDecoder, deriveEncoder, renaming}
import io.circe.syntax._

import scala.concurrent.duration.FiniteDuration

trait Encoders {
  implicit def dataChangeEncoder[T](implicit encoder: Encoder[T]): Encoder[DataChange[T]] =
    deriveEncoder(renaming.snakeCase)
  implicit def businessEncoder[T](implicit encoder: Encoder[T]): Encoder[Business[T]] =
    deriveEncoder(renaming.snakeCase)
  implicit def undefinedEncoder[T](implicit encoder: Encoder[T]): Encoder[Undefined[T]] =
    deriveEncoder(renaming.snakeCase)
  implicit def eventEncoder[T](implicit encoder: Encoder[T]): Encoder[Event[T]] =
    Encoder.instance[Event[T]] {
      case e: Event.DataChange[T] => e.asJson
      case e: Event.Business[T]   => e.asJson
      case e: Event.Undefined[T]  => e.asJson
    }

  implicit lazy val dataOpEncoder: Encoder[DataOperation] = enumeratum.Circe.encoder(DataOperation)
  implicit lazy val metadataEncoder: Encoder[Metadata]    = deriveEncoder(renaming.snakeCase)
  implicit lazy val eventIdEncoder: Encoder[EventId]      = Encoder.instance[EventId](_.id.asJson)
  implicit lazy val partitionCompactionKeyEncoder: Encoder[PartitionCompactionKey] =
    Encoder.instance[PartitionCompactionKey](_.key.asJson)
  implicit lazy val spanCtxEncoder: Encoder[SpanCtx]             = Encoder.instance[SpanCtx](_.ctx.asJson)
  implicit lazy val writeScopeEncoder: Encoder[WriteScope]       = Encoder.instance[WriteScope](_.id.asJson)
  implicit lazy val readScopeEncoder: Encoder[ReadScope]         = Encoder.instance[ReadScope](_.id.asJson)
  implicit lazy val eventTypeNameEncoder: Encoder[EventTypeName] = Encoder.instance[EventTypeName](_.name.asJson)
  implicit lazy val audienceEncoder: Encoder[Audience]           = enumeratum.Circe.encoder(Audience)
  implicit lazy val categoryEncoder: Encoder[Category]           = enumeratum.Circe.encoder(Category)
  implicit lazy val enrichmentStrategyEncoder: Encoder[EnrichmentStrategy] =
    enumeratum.Circe.encoder(EnrichmentStrategy)
  implicit lazy val partitionStrategyEncoder: Encoder[PartitionStrategy]           = enumeratum.Circe.encoder(PartitionStrategy)
  implicit lazy val cleanupPolicyEncoder: Encoder[CleanupPolicy]                   = enumeratum.Circe.encoder(CleanupPolicy)
  implicit lazy val compatibilityModeEncoder: Encoder[CompatibilityMode]           = enumeratum.Circe.encoder(CompatibilityMode)
  implicit lazy val typeEncoder: Encoder[Type]                                     = enumeratum.Circe.encoder(Type)
  implicit lazy val eventTypeSchemaEncoder: Encoder[EventTypeSchema]               = deriveEncoder(renaming.snakeCase)
  implicit lazy val eventTypeStatisticsEncoder: Encoder[EventTypeStatistics]       = deriveEncoder(renaming.snakeCase)
  implicit lazy val authorizationAttributeEncoder: Encoder[AuthorizationAttribute] = deriveEncoder(renaming.snakeCase)
  implicit lazy val eventTypeAuthorizationEncoder: Encoder[EventTypeAuthorization] = deriveEncoder(renaming.snakeCase)
  implicit lazy val eventTypeOptionsEncoder: Encoder[EventTypeOptions]             = deriveEncoder(renaming.snakeCase)
  implicit lazy val eventTypeEncoder: Encoder[EventType]                           = deriveEncoder(renaming.snakeCase)
  implicit lazy val flowIdEncoder: Encoder[FlowId]                                 = Encoder.instance[FlowId](_.id.asJson)
  implicit lazy val streamIdEncoder: Encoder[StreamId]                             = Encoder.instance[StreamId](_.id.asJson)
  implicit lazy val subscriptionIdEncoder: Encoder[SubscriptionId]                 = Encoder.instance[SubscriptionId](_.id.asJson)
  implicit lazy val subscriptionAuthorizationEncoder: Encoder[SubscriptionAuthorization] = deriveEncoder(
    renaming.snakeCase)
  implicit lazy val subscriptionEncoder: Encoder[Subscription] = deriveEncoder(renaming.snakeCase)
  implicit lazy val subscriptionQueryEncoder: Encoder[SubscriptionQuery] =
    Encoder.forProduct2(
      "_links",
      "items"
    )(x => SubscriptionQuery.unapply(x).get)
  implicit lazy val paginationLinkEncoder: Encoder[PaginationLink]               = Encoder.forProduct1("href")(_.href)
  implicit lazy val paginationLinksEncoder: Encoder[PaginationLinks]             = deriveEncoder(renaming.snakeCase)
  implicit lazy val subscriptionCursorEncoder: Encoder[SubscriptionCursor]       = deriveEncoder(renaming.snakeCase)
  implicit lazy val subscriptionEventInfoEncoder: Encoder[SubscriptionEventInfo] = deriveEncoder(renaming.snakeCase)
  implicit def subscriptionEventDataEncoder[T: Encoder]: Encoder[SubscriptionEventData[T]] =
    deriveEncoder(renaming.snakeCase)
  implicit def subscriptionEventEncoder[T: Encoder]: Encoder[SubscriptionEvent[T]] = deriveEncoder(renaming.snakeCase)
  implicit lazy val subscriptionStatsEncoder: Encoder[SubscriptionStats]           = deriveEncoder(renaming.snakeCase)
  implicit lazy val commitCursorItemResponseEncoder: Encoder[CommitCursorItemResponse] = deriveEncoder(
    renaming.snakeCase)
  implicit lazy val commitCursorResponseEncoder: Encoder[CommitCursorResponse] = deriveEncoder(renaming.snakeCase)
  implicit lazy val cursorEncoder: Encoder[Cursor]                             = deriveEncoder(renaming.snakeCase)
  implicit lazy val partitionEncoder: Encoder[Partition]                       = Encoder.instance[Partition](_.id.asJson)
  implicit lazy val cursorTokenEncoder: Encoder[CursorToken]                   = Encoder.instance[CursorToken](_.id.asJson)
  implicit lazy val stateEncoder: Encoder[State]                               = enumeratum.Circe.encoder(State)
  implicit lazy val assignmentTypeEncoder: Encoder[AssignmentType]             = enumeratum.Circe.encoder(AssignmentType)
  implicit lazy val eventTypeStatsPartitionEncoder: Encoder[EventTypeStatsPartition] = {
    implicit lazy val finiteDurationSecondsEncoder: Encoder[FiniteDuration] =
      Encoder.instance[FiniteDuration](_.toSeconds.asJson)

    deriveEncoder(renaming.snakeCase)
  }
  implicit lazy val eventTypeStatsEncoder: Encoder[EventTypeStats] = deriveEncoder(renaming.snakeCase)
  implicit lazy val uriEncoder: Encoder[URI]                       = Encoder.instance[URI](_.toString.asJson)
  implicit lazy val batchItemResponseEncoder: Encoder[BatchItemResponse] =
    Encoder.forProduct4(
      "eid",
      "publishing_status",
      "step",
      "detail"
    )(x => BatchItemResponse.unapply(x).get)
  implicit lazy val publishingStatusEncoder: Encoder[PublishingStatus] = enumeratum.Circe.encoder(PublishingStatus)
  implicit lazy val stepEncoder: Encoder[Step]                         = enumeratum.Circe.encoder(Step)

  implicit lazy val problemEncoder: Encoder[Problem] = deriveEncoder(renaming.snakeCase)
}
