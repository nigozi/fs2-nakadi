package fs2.nakadi.model
import java.time.OffsetDateTime
import java.util.UUID

import enumeratum.{Enum, EnumEntry}
import io.circe.derivation._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, JsonObject}

import scala.collection.immutable
import scala.concurrent.duration._

final case class SubscriptionId(id: UUID) extends AnyVal

object SubscriptionId {
  implicit val encoder: Encoder[SubscriptionId] = Encoder.instance[SubscriptionId](_.id.asJson)
  implicit val decoder: Decoder[SubscriptionId] = Decoder[UUID].map(SubscriptionId.apply)
}

final case class SubscriptionAuthorization(admins: List[AuthorizationAttribute], readers: List[AuthorizationAttribute])

object SubscriptionAuthorization {
  implicit val encoder: Encoder[SubscriptionAuthorization] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[SubscriptionAuthorization] = deriveDecoder(renaming.snakeCase)
}

final case class Subscription(id: Option[SubscriptionId] = None,
                              owningApplication: String,
                              eventTypes: Option[List[EventTypeName]] = None,
                              consumerGroup: Option[String] = None,
                              createdAt: Option[OffsetDateTime] = None,
                              readFrom: Option[String] = None,
                              initialCursors: Option[List[String]] = None,
                              authorization: Option[SubscriptionAuthorization] = None)

object Subscription {
  implicit val encoder: Encoder[Subscription] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[Subscription] = deriveDecoder(renaming.snakeCase)
}

final case class SubscriptionQuery(links: PaginationLinks, items: List[Subscription])

object SubscriptionQuery {
  implicit val encoder: Encoder[SubscriptionQuery] =
    Encoder.forProduct2(
      "_links",
      "items"
    )(x => SubscriptionQuery.unapply(x).get)

  implicit val decoder: Decoder[SubscriptionQuery] =
    Decoder.forProduct2(
      "_links",
      "items"
    )(SubscriptionQuery.apply)
}

final case class SubscriptionCursor(items: List[Cursor])

object SubscriptionCursor {
  implicit val encoder: Encoder[SubscriptionCursor] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[SubscriptionCursor] = deriveDecoder(renaming.snakeCase)
}

final case class SubscriptionEventInfo(cursor: Cursor, info: Option[JsonObject])

object SubscriptionEventInfo {
  implicit val encoder: Encoder[SubscriptionEventInfo] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[SubscriptionEventInfo] = deriveDecoder(renaming.snakeCase)
}

final case class SubscriptionEventData[T](events: Option[List[Event[T]]])

object SubscriptionEventData {
  implicit def encoder[T: Encoder]: Encoder[SubscriptionEventData[T]] = deriveEncoder(renaming.snakeCase)
  implicit def decoder[T: Decoder]: Decoder[SubscriptionEventData[T]] = deriveDecoder(renaming.snakeCase)
}

final case class SubscriptionEvent[T](cursor: Cursor, info: Option[JsonObject], events: Option[List[Event[T]]])

object SubscriptionEvent {
  implicit def encoder[T: Encoder]: Encoder[SubscriptionEvent[T]] = deriveEncoder(renaming.snakeCase)
  implicit def decoder[T: Decoder]: Decoder[SubscriptionEvent[T]] = deriveDecoder(renaming.snakeCase)
}

final case class SubscriptionStats(items: List[EventTypeStats])

object SubscriptionStats {
  implicit val encoder: Encoder[SubscriptionStats] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[SubscriptionStats] = deriveDecoder(renaming.snakeCase)
}

final case class CommitCursorItemResponse(cursor: Cursor, result: String)

object CommitCursorItemResponse {
  implicit val encoder: Encoder[CommitCursorItemResponse] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[CommitCursorItemResponse] = deriveDecoder(renaming.snakeCase)
}

final case class CommitCursorResponse(items: List[CommitCursorItemResponse])

object CommitCursorResponse {
  implicit val encoder: Encoder[CommitCursorResponse] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[CommitCursorResponse] = deriveDecoder(renaming.snakeCase)
}

final case class Cursor(partition: Partition, offset: String, eventType: EventTypeName, cursorToken: CursorToken)

object Cursor {
  implicit val encoder: Encoder[Cursor] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[Cursor] = deriveDecoder(renaming.snakeCase)
}

final case class CursorToken(id: UUID) extends AnyVal

object CursorToken {
  implicit val encoder: Encoder[CursorToken] = Encoder.instance[CursorToken](_.id.asJson)
  implicit val decoder: Decoder[CursorToken] = Decoder[UUID].map(CursorToken.apply)
}

final case class EventTypeStats(eventType: EventTypeName, partitions: List[EventTypeStats.Partition])

object EventTypeStats {
  final case class Partition(partition: Partition,
                             state: Partition.State,
                             unconsumedEvents: Option[Int],
                             consumerLagSeconds: Option[FiniteDuration],
                             streamId: Option[StreamId],
                             assignmentType: Option[Partition.AssignmentType])

  object Partition {
    sealed abstract class State(val id: String) extends EnumEntry with Product with Serializable {
      override val entryName: String = id
    }

    object State extends Enum[State] {
      val values: immutable.IndexedSeq[State] = findValues

      case object Unassigned  extends State("unassigned")
      case object Reassigning extends State("reassigning")
      case object Assigned    extends State("assigned")

      implicit val encoder: Encoder[State] = enumeratum.Circe.encoder(State)
      implicit val decoder: Decoder[State] = enumeratum.Circe.decoder(State)
    }

    sealed abstract class AssignmentType(val id: String) extends EnumEntry with Product with Serializable {
      override val entryName: String = id
    }

    object AssignmentType extends Enum[AssignmentType] {
      val values: immutable.IndexedSeq[AssignmentType] = findValues

      case object Direct extends AssignmentType("direct")
      case object Auto   extends AssignmentType("auto")

      implicit val encoder: Encoder[AssignmentType] = enumeratum.Circe.encoder(AssignmentType)
      implicit val decoder: Decoder[AssignmentType] = enumeratum.Circe.decoder(AssignmentType)

    }

    implicit val encoder: Encoder[Partition] = {
      implicit val finiteDurationSecondsEncoder: Encoder[FiniteDuration] =
        Encoder.instance[FiniteDuration](_.toSeconds.asJson)

      deriveEncoder(renaming.snakeCase)
    }

    implicit val decoder: Decoder[Partition] = {
      implicit val finiteDurationSecondsDecoder: Decoder[FiniteDuration] =
        Decoder[Long].map(seconds => FiniteDuration(seconds, scala.concurrent.duration.SECONDS))

      deriveDecoder(renaming.snakeCase)
    }
  }

  implicit val encoder: Encoder[EventTypeStats] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[EventTypeStats] = deriveDecoder(renaming.snakeCase)
}

final case class StreamConfig(
    maxUncommittedEvents: Option[Int] = None,
    batchLimit: Option[Int] = None,
    streamLimit: Option[Int] = None,
    batchFlushTimeout: Option[FiniteDuration] = None,
    commitTimeout: Option[FiniteDuration] = None,
    streamTimeout: Option[FiniteDuration] = None,
    streamKeepAliveLimit: Option[Int] = None,
    noEmptySlotsRetryDelay: FiniteDuration = 30.seconds
)

final case class StreamEvent[T](event: SubscriptionEvent[T], streamId: StreamId)
