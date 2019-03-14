package fs2.nakadi.model

import java.time.OffsetDateTime
import java.util.UUID

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class Event[T](val data: T)

object Event {
  final case class DataChange[T](override val data: T, dataType: String, dataOp: DataOperation, metadata: Metadata)
      extends Event[T](data)
  final case class Business[T](override val data: T, metadata: Metadata = Metadata()) extends Event[T](data)
  final case class Undefined[T](override val data: T)                                 extends Event[T](data)
}

sealed abstract class DataOperation(val id: String) extends EnumEntry with Product with Serializable {
  override val entryName: String = id
}

object DataOperation extends Enum[DataOperation] {
  val values: immutable.IndexedSeq[DataOperation] = findValues
  case object Create   extends DataOperation("C")
  case object Update   extends DataOperation("U")
  case object Delete   extends DataOperation("D")
  case object Snapshot extends DataOperation("S")
}

final case class Metadata(eid: EventId = EventId.random,
                          occurredAt: OffsetDateTime = OffsetDateTime.now,
                          eventType: Option[EventTypeName] = None,
                          receivedAt: Option[OffsetDateTime] = None,
                          parentEids: Option[List[EventId]] = None,
                          flowId: Option[FlowId] = None,
                          partition: Option[Partition] = None,
                          partitionCompactionKey: Option[PartitionCompactionKey] = None,
                          spanCtx: Option[SpanCtx] = None)

final case class EventId(id: String) extends AnyVal

object EventId {
  def random: EventId = EventId(java.util.UUID.randomUUID().toString)
}

final case class PartitionCompactionKey(key: String) extends AnyVal

object PartitionCompactionKey {
  def random: PartitionCompactionKey = PartitionCompactionKey(UUID.randomUUID().toString)
}

final case class SpanCtx(ctx: Map[String, String]) extends AnyVal
