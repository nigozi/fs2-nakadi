package fs2.nakadi.model

import java.time.OffsetDateTime

import cats.effect.Sync
import enumeratum.{Enum, EnumEntry}
import io.circe.Decoder.Result
import io.circe.derivation._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

import scala.collection.immutable

sealed abstract class Event[T](val data: T)

object Event {
  final case class DataChange[T](override val data: T, dataType: String, dataOp: DataOperation, metadata: Metadata)
      extends Event[T](data)

  object DataChange {
    implicit def encoder[T](implicit encoder: Encoder[T]): Encoder[DataChange[T]] = deriveEncoder(renaming.snakeCase)
    implicit def decoder[T](implicit decoder: Decoder[T]): Decoder[DataChange[T]] = deriveDecoder(renaming.snakeCase)
  }

  final case class Business[T](override val data: T, metadata: Metadata = Metadata()) extends Event[T](data)

  object Business {
    implicit def encoder[T](implicit encoder: Encoder[T]): Encoder[Business[T]] = deriveEncoder(renaming.snakeCase)
    implicit def decoder[T](implicit decoder: Decoder[T]): Decoder[Business[T]] =
      deriveDecoder(renaming.snakeCase)
  }

  final case class Undefined[T](override val data: T) extends Event[T](data)

  object Undefined {
    implicit def encoder[T](implicit encoder: Encoder[T]): Encoder[Undefined[T]] = deriveEncoder(renaming.snakeCase)
    implicit def decoder[T](implicit decoder: Decoder[T]): Decoder[Undefined[T]] =
      deriveDecoder(renaming.snakeCase)
  }

  implicit def encoder[T](implicit encoder: Encoder[T]): Encoder[Event[T]] =
    Encoder.instance[Event[T]] {
      case e: Event.DataChange[T] => e.asJson
      case e: Event.Business[T]   => e.asJson
      case e: Event.Undefined[T]  => e.asJson
    }

  implicit def decoder[T](implicit decoder: Decoder[T]): Decoder[Event[T]] =
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

  implicit def entityEncoder[F[_]: Sync, T](implicit enc: Encoder[Event[T]]): EntityEncoder[F, Event[T]] =
    jsonEncoderOf[F, Event[T]]
  implicit def entityDecoder[F[_]: Sync, T](implicit dec: Decoder[Event[T]]): EntityDecoder[F, Event[T]] =
    jsonOf[F, Event[T]]
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

  implicit val encoder: Encoder[DataOperation] =
    enumeratum.Circe.encoder(DataOperation)
  implicit val decoder: Decoder[DataOperation] =
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
  implicit val encoder: Encoder[Metadata] = deriveEncoder(renaming.snakeCase)
  implicit val decoder: Decoder[Metadata] = deriveDecoder(renaming.snakeCase)
}
