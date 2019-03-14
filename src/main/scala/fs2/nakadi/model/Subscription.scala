package fs2.nakadi.model
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

import enumeratum.{Enum, EnumEntry}
import fs2.nakadi.model.EventTypeStats.EventTypeStatsPartition
import io.circe.JsonObject

import scala.collection.immutable
import scala.concurrent.duration._

final case class SubscriptionId(id: UUID) extends AnyVal

final case class SubscriptionAuthorization(admins: List[AuthorizationAttribute], readers: List[AuthorizationAttribute])

final case class Subscription(id: Option[SubscriptionId] = None,
                              owningApplication: String,
                              eventTypes: Option[List[EventTypeName]] = None,
                              consumerGroup: Option[String] = None,
                              createdAt: Option[OffsetDateTime] = None,
                              readFrom: Option[String] = None,
                              initialCursors: Option[List[String]] = None,
                              authorization: Option[SubscriptionAuthorization] = None)

final case class SubscriptionQuery(links: PaginationLinks, items: List[Subscription])

final case class PaginationLink(href: URI) extends AnyVal

final case class PaginationLinks(prev: Option[PaginationLink], next: Option[PaginationLink])

final case class SubscriptionCursor(items: List[Cursor])

final case class SubscriptionEventInfo(cursor: Cursor, info: Option[JsonObject])

final case class SubscriptionEventData[T](events: Option[List[Event[T]]])

final case class SubscriptionEvent[T](cursor: Cursor, info: Option[JsonObject], events: Option[List[Event[T]]])

final case class SubscriptionStats(items: List[EventTypeStats])

final case class CommitCursorItemResponse(cursor: Cursor, result: String)

final case class CommitCursorResponse(items: List[CommitCursorItemResponse])

final case class Cursor(partition: Partition, offset: String, eventType: EventTypeName, cursorToken: CursorToken)

final case class Partition(id: String) extends AnyVal

final case class CursorToken(id: UUID) extends AnyVal

final case class EventTypeStats(eventType: EventTypeName, partitions: List[EventTypeStatsPartition])

object EventTypeStats {
  final case class EventTypeStatsPartition(partition: Partition,
                             state: EventTypeStatsPartition.State,
                             unconsumedEvents: Option[Int],
                             consumerLagSeconds: Option[FiniteDuration],
                             streamId: Option[StreamId],
                             assignmentType: Option[EventTypeStatsPartition.AssignmentType])

  object EventTypeStatsPartition {
    sealed abstract class State(val id: String) extends EnumEntry with Product with Serializable {
      override val entryName: String = id
    }

    object State extends Enum[State] {
      val values: immutable.IndexedSeq[State] = findValues

      case object Unassigned  extends State("unassigned")
      case object Reassigning extends State("reassigning")
      case object Assigned    extends State("assigned")
    }

    sealed abstract class AssignmentType(val id: String) extends EnumEntry with Product with Serializable {
      override val entryName: String = id
    }

    object AssignmentType extends Enum[AssignmentType] {
      val values: immutable.IndexedSeq[AssignmentType] = findValues

      case object Direct extends AssignmentType("direct")
      case object Auto   extends AssignmentType("auto")
    }
  }
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
