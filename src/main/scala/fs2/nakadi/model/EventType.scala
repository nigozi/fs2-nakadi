package fs2.nakadi.model

import java.time.OffsetDateTime

import enumeratum._
import io.circe.syntax._
import io.circe.{Json, JsonObject}

import scala.collection.immutable

final case class WriteScope(id: String) extends AnyVal

final case class ReadScope(id: String) extends AnyVal

final case class EventTypeName(name: String) extends AnyVal

sealed abstract class Audience(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object Audience extends Enum[Audience] {
  val values: immutable.IndexedSeq[Audience] = findValues
  case object BusinessUnitInternal extends Audience("business-unit-internal")
  case object CompanyInternal      extends Audience("company-internal")
  case object ComponentInternal    extends Audience("component-internal")
  case object ExternalPartner      extends Audience("external-partner")
  case object ExternalPublic       extends Audience("external-public")
}

sealed abstract class Category(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object Category extends Enum[Category] {
  val values: immutable.IndexedSeq[Category] = findValues
  case object Business  extends Category("business")
  case object Data      extends Category("data")
  case object Undefined extends Category("undefined")
}

sealed abstract class EnrichmentStrategy(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object EnrichmentStrategy extends Enum[EnrichmentStrategy] {
  val values: immutable.IndexedSeq[EnrichmentStrategy] = findValues
  case object MetadataEnrichment extends EnrichmentStrategy("metadata_enrichment")
}

sealed abstract class PartitionStrategy(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object PartitionStrategy extends Enum[PartitionStrategy] {
  val values: immutable.IndexedSeq[PartitionStrategy] = findValues
  case object Random      extends PartitionStrategy("random")
  case object UserDefined extends PartitionStrategy("user_defined")
  case object Hash        extends PartitionStrategy("hash")
}

sealed abstract class CleanupPolicy(val id: String) extends EnumEntry with Product with Serializable {
  override def entryName: String = id
}

object CleanupPolicy extends Enum[CleanupPolicy] {
  val values: immutable.IndexedSeq[CleanupPolicy] = findValues
  case object Compact extends CleanupPolicy("compact")
  case object Delete  extends CleanupPolicy("delete")
}

sealed abstract class CompatibilityMode(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object CompatibilityMode extends Enum[CompatibilityMode] {
  val values: immutable.IndexedSeq[CompatibilityMode] = findValues
  case object Compatible extends CompatibilityMode("compatible")
  case object Forward    extends CompatibilityMode("forward")
  case object None       extends CompatibilityMode("none")
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
    override val entryName: String = id
  }

  object Type extends Enum[Type] {
    val values: immutable.IndexedSeq[Type] = findValues

    case object JsonSchema extends Type("json_schema")
  }
}

final case class EventTypeStatistics(messagesPerMinute: Int,
                                     messageSize: Int,
                                     readParallelism: Int,
                                     writeParallelism: Int)

final case class AuthorizationAttribute(dataType: String, value: String)

final case class EventTypeAuthorization(admins: List[AuthorizationAttribute],
                                        readers: List[AuthorizationAttribute],
                                        writers: List[AuthorizationAttribute])

final case class EventTypeOptions(retentionTime: Int)

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
