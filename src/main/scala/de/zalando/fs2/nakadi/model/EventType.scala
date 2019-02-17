package de.zalando.fs2.nakadi.model
import java.time.OffsetDateTime

import enumeratum._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, JsonObject}

final case class WriteScope(id: String) extends AnyVal

object WriteScope {
  implicit val writeScopeEncoder: Encoder[WriteScope] =
    Encoder.instance[WriteScope](_.id.asJson)
  implicit val writeScopeDecoder: Decoder[WriteScope] =
    Decoder[String].map(WriteScope.apply)
}

final case class ReadScope(id: String) extends AnyVal

object ReadScope {
  implicit val readScopeEncoder: Encoder[ReadScope] =
    Encoder.instance[ReadScope](_.id.asJson)
  implicit val readScopeDecoder: Decoder[ReadScope] =
    Decoder[String].map(ReadScope.apply)
}

final case class EventTypeName(name: String) extends AnyVal

object EventTypeName {
  implicit val eventTypeNameEncoder: Encoder[EventTypeName] =
    Encoder.instance[EventTypeName](_.name.asJson)
  implicit val eventTypeNameDecoder: Decoder[EventTypeName] =
    Decoder[String].map(EventTypeName.apply)
}

sealed abstract class Audience(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName = id
}

object Audience extends Enum[Audience] {
  val values = findValues
  case object BusinessUnitInternal extends Audience("business-unit-internal")
  case object CompanyInternal      extends Audience("company-internal")
  case object ComponentInternal    extends Audience("component-internal")
  case object ExternalPartner      extends Audience("external-partner")
  case object ExternalPublic       extends Audience("external-public")

  implicit val audienceEncoder: Encoder[Audience] =
    enumeratum.Circe.encoder(Audience)

  implicit val audienceDecoder: Decoder[Audience] =
    enumeratum.Circe.decoder(Audience)
}

sealed abstract class Category(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName = id
}

object Category extends Enum[Category] {
  val values = findValues
  case object Business  extends Category("business")
  case object Data      extends Category("data")
  case object Undefined extends Category("undefined")

  implicit val categoryEncoder: Encoder[Category] =
    enumeratum.Circe.encoder(Category)

  implicit val categoryDecoder: Decoder[Category] =
    enumeratum.Circe.decoder(Category)
}

sealed abstract class EnrichmentStrategy(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName = id
}

object EnrichmentStrategy extends Enum[EnrichmentStrategy] {
  val values = findValues
  case object MetadataEnrichment extends EnrichmentStrategy("metadata_enrichment")

  implicit val enrichmentStrategyEncoder: Encoder[EnrichmentStrategy] =
    enumeratum.Circe.encoder(EnrichmentStrategy)
  implicit val enrichmentStrategyDecoder: Decoder[EnrichmentStrategy] =
    enumeratum.Circe.decoder(EnrichmentStrategy)
}

sealed abstract class PartitionStrategy(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName = id
}

object PartitionStrategy extends Enum[PartitionStrategy] {
  val values = findValues
  case object Random      extends PartitionStrategy("random")
  case object UserDefined extends PartitionStrategy("user_defined")
  case object Hash        extends PartitionStrategy("hash")

  implicit val partitionStrategyEncoder: Encoder[PartitionStrategy] =
    enumeratum.Circe.encoder(PartitionStrategy)
  implicit val partitionStrategyDecoder: Decoder[PartitionStrategy] =
    enumeratum.Circe.decoder(PartitionStrategy)
}

sealed abstract class CleanupPolicy(val id: String) extends EnumEntry with Product with Serializable {
  override def entryName = id
}

object CleanupPolicy extends Enum[CleanupPolicy] {
  val values = findValues
  case object Compact extends CleanupPolicy("compact")
  case object Delete  extends CleanupPolicy("delete")

  implicit val cleanupPolicyEncoder: Encoder[CleanupPolicy] =
    enumeratum.Circe.encoder(CleanupPolicy)
  implicit val cleanupPolicyDecoder: Decoder[CleanupPolicy] =
    enumeratum.Circe.decoder(CleanupPolicy)
}

sealed abstract class CompatibilityMode(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName = id
}

object CompatibilityMode extends Enum[CompatibilityMode] {
  val values = findValues
  case object Compatible extends CompatibilityMode("compatible")
  case object Forward    extends CompatibilityMode("forward")
  case object None       extends CompatibilityMode("none")

  implicit val compatibilityModeEncoder: Encoder[CompatibilityMode] =
    enumeratum.Circe.encoder(CompatibilityMode)

  implicit val compatibilityModeDecoder: Decoder[CompatibilityMode] =
    enumeratum.Circe.decoder(CompatibilityMode)
}

final case class EventTypeSchema(version: Option[String],
                                 createdAt: Option[OffsetDateTime],
                                 `type`: EventTypeSchema.Type,
                                 schema: Json)

object EventTypeSchema {

  val anyJsonObject = EventTypeSchema(
    None,
    None,
    EventTypeSchema.Type.JsonSchema,
    JsonObject.singleton("type", "object".asJson).asJson.noSpaces.asJson
  )

  sealed abstract class Type(val id: String) extends EnumEntry with Product with Serializable {
    override val entryName = id
  }

  object Type extends Enum[Type] {
    val values = findValues

    case object JsonSchema extends Type("json_schema")

    implicit val eventTypeSchemaTypeEncoder: Encoder[Type] =
      enumeratum.Circe.encoder(Type)
    implicit val eventTypeSchemaTypeDecoder: Decoder[Type] =
      enumeratum.Circe.decoder(Type)
  }

  implicit val eventTypeSchemaEncoder: Encoder[EventTypeSchema] =
    Encoder.forProduct4("version", "created_at", "type", "schema")(x => EventTypeSchema.unapply(x).get)

  implicit val eventTypeSchemaDecoder: Decoder[EventTypeSchema] =
    Decoder.forProduct4("version", "created_at", "type", "schema")(EventTypeSchema.apply)
}

final case class EventTypeStatistics(messagesPerMinute: Int,
                                     messageSize: Int,
                                     readParallelism: Int,
                                     writeParallelism: Int)

object EventTypeStatistics {
  implicit val eventTypeStatisticsEncoder: Encoder[EventTypeStatistics] =
    Encoder.forProduct4(
      "messages_per_minute",
      "message_size",
      "read_parallelism",
      "write_parallelism"
    )(x => EventTypeStatistics.unapply(x).get)

  implicit val eventTypeStatisticsDecoder: Decoder[EventTypeStatistics] =
    Decoder.forProduct4(
      "messages_per_minute",
      "message_size",
      "read_parallelism",
      "write_parallelism"
    )(EventTypeStatistics.apply)
}

final case class AuthorizationAttribute(dataType: String, value: String)

object AuthorizationAttribute {
  implicit val eventTypeAuthorizationAuthorizationAttributeEncoder: Encoder[AuthorizationAttribute] =
    Encoder.forProduct2(
      "data_type",
      "value"
    )(x => AuthorizationAttribute.unapply(x).get)

  implicit val eventTypeAuthorizationAuthorizationAttributeDecoder: Decoder[AuthorizationAttribute] =
    Decoder.forProduct2(
      "data_type",
      "value"
    )(AuthorizationAttribute.apply)
}

final case class EventTypeAuthorization(admins: List[AuthorizationAttribute],
                                        readers: List[AuthorizationAttribute],
                                        writers: List[AuthorizationAttribute])

object EventTypeAuthorization {
  implicit val eventTypeAuthorizationEncoder: Encoder[EventTypeAuthorization] =
    Encoder.forProduct3(
      "admins",
      "readers",
      "writers"
    )(x => EventTypeAuthorization.unapply(x).get)

  implicit val eventTypeAuthorizationDecoder: Decoder[EventTypeAuthorization] =
    Decoder.forProduct3(
      "admins",
      "readers",
      "writers"
    )(EventTypeAuthorization.apply)
}

final case class EventTypeOptions(retentionTime: Int)

object EventTypeOptions {
  implicit val eventTypeOptionsEncoder: Encoder[EventTypeOptions] =
    Encoder.forProduct1(
      "retention_time"
    )(x => EventTypeOptions.unapply(x).get)

  implicit val eventTypeOptionsDecoder: Decoder[EventTypeOptions] =
    Decoder.forProduct1(
      "retention_time"
    )(EventTypeOptions.apply)
}

final case class EventType(
    name: EventTypeName,
    owningApplication: String,
    category: Category,
    enrichmentStrategies: List[EnrichmentStrategy] = List(EnrichmentStrategy.MetadataEnrichment),
    partitionStrategy: Option[PartitionStrategy] = None,
    compatibilityMode: Option[CompatibilityMode] = None,
    schema: EventTypeSchema = EventTypeSchema.anyJsonObject,
    partitionKeyFields: Option[List[String]] = None,
    cleanupPolicy: Option[CleanupPolicy] = None,
    defaultStatistic: Option[EventTypeStatistics] = None,
    options: Option[EventTypeOptions] = None,
    authorization: Option[EventTypeAuthorization] = None,
    writeScopes: Option[List[WriteScope]] = None,
    readScopes: Option[List[ReadScope]] = None,
    audience: Option[Audience] = None,
    orderingKeyFields: Option[List[String]] = None,
    orderingInstanceIds: Option[List[String]] = None,
    createdAt: Option[OffsetDateTime] = None,
    updatedAt: Option[OffsetDateTime] = None
)

object EventType {
  implicit val eventTypeEncoder: Encoder[EventType] = Encoder.forProduct19(
    "name",
    "owning_application",
    "category",
    "enrichment_strategies",
    "partition_strategy",
    "compatibility_mode",
    "schema",
    "partition_key_fields",
    "cleanup_policy",
    "default_statistic",
    "options",
    "authorization",
    "write_scopes",
    "read_scopes",
    "audience",
    "ordering_key_fields",
    "ordering_instance_ids",
    "created_at",
    "updated_at"
  )(x => EventType.unapply(x).get)

  implicit val eventTypeDecoder: Decoder[EventType] = Decoder.forProduct19(
    "name",
    "owning_application",
    "category",
    "enrichment_strategies",
    "partition_strategy",
    "compatibility_mode",
    "schema",
    "partition_key_fields",
    "cleanup_policy",
    "default_statistic",
    "options",
    "authorization",
    "write_scopes",
    "read_scopes",
    "audience",
    "ordering_key_fields",
    "ordering_instance_ids",
    "created_at",
    "updated_at"
  )(EventType.apply)
}
