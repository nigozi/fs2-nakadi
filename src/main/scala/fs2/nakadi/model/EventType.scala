package fs2.nakadi.model

import java.time.OffsetDateTime

import cats.effect.Sync
import enumeratum._
import io.circe.derivation._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, JsonObject}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

import scala.collection.immutable

final case class WriteScope(id: String) extends AnyVal

object WriteScope {
  implicit val encoder: Encoder[WriteScope] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[WriteScope] = deriveDecoder(renaming.snakeCase)
}

final case class ReadScope(id: String) extends AnyVal

object ReadScope {
  implicit val encoder: Encoder[ReadScope] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[ReadScope] = deriveDecoder(renaming.snakeCase)
}

final case class EventTypeName(name: String) extends AnyVal

object EventTypeName {
  implicit val eventTypeNameEncoder: Encoder[EventTypeName] =
    Encoder.instance[EventTypeName](_.name.asJson)
  implicit val eventTypeNameDecoder: Decoder[EventTypeName] =
    Decoder[String].map(EventTypeName.apply)
}

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

  implicit val encoder: Encoder[Audience] = enumeratum.Circe.encoder(Audience)
  implicit val decoder: Decoder[Audience] = enumeratum.Circe.decoder(Audience)
}

sealed abstract class Category(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object Category extends Enum[Category] {
  val values: immutable.IndexedSeq[Category] = findValues
  case object Business  extends Category("business")
  case object Data      extends Category("data")
  case object Undefined extends Category("undefined")

  implicit val encoder: Encoder[Category] = enumeratum.Circe.encoder(Category)
  implicit val decoder: Decoder[Category] = enumeratum.Circe.decoder(Category)
}

sealed abstract class EnrichmentStrategy(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object EnrichmentStrategy extends Enum[EnrichmentStrategy] {
  val values: immutable.IndexedSeq[EnrichmentStrategy] = findValues
  case object MetadataEnrichment extends EnrichmentStrategy("metadata_enrichment")

  implicit val encoder: Encoder[EnrichmentStrategy] = enumeratum.Circe.encoder(EnrichmentStrategy)
  implicit val decoder: Decoder[EnrichmentStrategy] = enumeratum.Circe.decoder(EnrichmentStrategy)

  implicit def entityEncoder[F[_]: Sync]: EntityEncoder[F, EnrichmentStrategy] = jsonEncoderOf[F, EnrichmentStrategy]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, EnrichmentStrategy] = jsonOf[F, EnrichmentStrategy]
}

sealed abstract class PartitionStrategy(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object PartitionStrategy extends Enum[PartitionStrategy] {
  val values: immutable.IndexedSeq[PartitionStrategy] = findValues
  case object Random      extends PartitionStrategy("random")
  case object UserDefined extends PartitionStrategy("user_defined")
  case object Hash        extends PartitionStrategy("hash")

  implicit val encoder: Encoder[PartitionStrategy] = enumeratum.Circe.encoder(PartitionStrategy)
  implicit val decoder: Decoder[PartitionStrategy] = enumeratum.Circe.decoder(PartitionStrategy)

  implicit def entityEncoder[F[_]: Sync]: EntityEncoder[F, PartitionStrategy] = jsonEncoderOf[F, PartitionStrategy]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, PartitionStrategy] = jsonOf[F, PartitionStrategy]
}

sealed abstract class CleanupPolicy(val id: String) extends EnumEntry with Product with Serializable {
  override def entryName: String = id
}

object CleanupPolicy extends Enum[CleanupPolicy] {
  val values: immutable.IndexedSeq[CleanupPolicy] = findValues
  case object Compact extends CleanupPolicy("compact")
  case object Delete  extends CleanupPolicy("delete")

  implicit val encoder: Encoder[CleanupPolicy] = enumeratum.Circe.encoder(CleanupPolicy)
  implicit val decoder: Decoder[CleanupPolicy] = enumeratum.Circe.decoder(CleanupPolicy)
}

sealed abstract class CompatibilityMode(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object CompatibilityMode extends Enum[CompatibilityMode] {
  val values: immutable.IndexedSeq[CompatibilityMode] = findValues
  case object Compatible extends CompatibilityMode("compatible")
  case object Forward    extends CompatibilityMode("forward")
  case object None       extends CompatibilityMode("none")

  implicit val encoder: Encoder[CompatibilityMode] = enumeratum.Circe.encoder(CompatibilityMode)
  implicit val decoder: Decoder[CompatibilityMode] = enumeratum.Circe.decoder(CompatibilityMode)
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

    implicit val encoder: Encoder[Type] = enumeratum.Circe.encoder(Type)
    implicit val decoder: Decoder[Type] = enumeratum.Circe.decoder(Type)
  }

  implicit val encoder: Encoder[EventTypeSchema] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventTypeSchema] = deriveDecoder(renaming.snakeCase)
}

final case class EventTypeStatistics(messagesPerMinute: Int,
                                     messageSize: Int,
                                     readParallelism: Int,
                                     writeParallelism: Int)

object EventTypeStatistics {
  implicit val encoder: Encoder[EventTypeStatistics] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventTypeStatistics] = deriveDecoder(renaming.snakeCase)
}

final case class AuthorizationAttribute(dataType: String, value: String)

object AuthorizationAttribute {
  implicit val encoder: Encoder[AuthorizationAttribute] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[AuthorizationAttribute] = deriveDecoder(renaming.snakeCase)
}

final case class EventTypeAuthorization(admins: List[AuthorizationAttribute],
                                        readers: List[AuthorizationAttribute],
                                        writers: List[AuthorizationAttribute])

object EventTypeAuthorization {
  implicit val encoder: Encoder[EventTypeAuthorization] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventTypeAuthorization] = deriveDecoder(renaming.snakeCase)
}

final case class EventTypeOptions(retentionTime: Int)

object EventTypeOptions {
  implicit val encoder: Encoder[EventTypeOptions] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventTypeOptions] = deriveDecoder(renaming.snakeCase)
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
  implicit val encoder: Encoder[EventType] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventType] = deriveDecoder(renaming.snakeCase)

  implicit def entityEncoder[F[_]: Sync]: EntityEncoder[F, EventType] = jsonEncoderOf[F, EventType]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, EventType] = jsonOf[F, EventType]
}
