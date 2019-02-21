package de.zalando.fs2.nakadi.model
import java.time.OffsetDateTime

import scala.collection.immutable

import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder, Json}
import io.circe.Decoder.Result
import io.circe.syntax._

sealed abstract class Event[T](val data: T)

object Event {
  final case class DataChange[T](override val data: T,
                                 dataType: String,
                                 dataOperation: DataOperation,
                                 metadata: Metadata)
      extends Event[T](data)

  object DataChange {
    implicit def eventDataChangeEncoder[T](implicit encoder: Encoder[T]): Encoder[DataChange[T]] =
      Encoder.forProduct4(
        "data",
        "data_type",
        "data_op",
        "metadata"
      )(x => DataChange.unapply(x).get)

    implicit def eventDataChangeDecoder[T](implicit decoder: Decoder[T]): Decoder[DataChange[T]] =
      Decoder.forProduct4(
        "data",
        "data_type",
        "data_op",
        "metadata"
      )(DataChange.apply)
  }

  final case class Business[T](override val data: T, metadata: Metadata = Metadata()) extends Event[T](data)

  object Business {
    implicit def eventBusinessEncoder[T](implicit encoder: Encoder[T]): Encoder[Business[T]] =
      Encoder.instance[Business[T]] { x =>
        val metadata = Json.obj(
          "metadata" -> x.metadata.asJson
        )
        val data = x.data.asJson
        data.deepMerge(metadata)
      }

    implicit def eventBusinessDecoder[T](
        implicit decoder: Decoder[T]
    ): Decoder[Business[T]] =
      Decoder.instance[Business[T]] { c =>
        for {
          metadata <- c.downField("metadata").as[Metadata]
          data     <- c.as[T]
        } yield Business(data, metadata)
      }
  }

  final case class Undefined[T](override val data: T) extends Event[T](data)

  object Undefined {
    implicit def eventUndefinedEncoder[T](implicit encoder: Encoder[T]): Encoder[Undefined[T]] =
      Encoder.instance[Undefined[T]] { x =>
        x.data.asJson
      }

    implicit def eventUndefinedDecoder[T](
        implicit decoder: Decoder[T]
    ): Decoder[Undefined[T]] =
      Decoder.instance[Undefined[T]] { c =>
        for {
          data <- c.as[T]
        } yield Undefined(data)
      }
  }

  implicit def eventEncoder[T](implicit encoder: Encoder[T]): Encoder[Event[T]] =
    Encoder.instance[Event[T]] {
      case e: Event.DataChange[T] => e.asJson
      case e: Event.Business[T]   => e.asJson
      case e: Event.Undefined[T]  => e.asJson
    }

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

  implicit val dataOperationEncoder: Encoder[DataOperation] =
    enumeratum.Circe.encoder(DataOperation)
  implicit val dataOperationDecoder: Decoder[DataOperation] =
    enumeratum.Circe.decoder(DataOperation)
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

object Metadata {

  implicit val metadataEncoder: Encoder[Metadata] = Encoder.forProduct9(
    "eid",
    "occurred_at",
    "event_type",
    "received_at",
    "parent_eids",
    "flow_id",
    "partition",
    "partition_compaction_key",
    "span_ctx"
  )(x => Metadata.unapply(x).get)

  implicit val metadataDecoder: Decoder[Metadata] = Decoder.forProduct9(
    "eid",
    "occurred_at",
    "event_type",
    "received_at",
    "parent_eids",
    "flow_id",
    "partition",
    "partition_compaction_key",
    "span_ctx"
  )(Metadata.apply)
}
