package de.zalando.fs2.nakadi.impl
import cats.Monad
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.typesafe.scalalogging.{Logger, LoggerTakingImplicit}
import org.http4s._
import org.http4s.Method._
import org.http4s.client.Client

import de.zalando.fs2.nakadi.model._
import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

class Events[F[_]: Monad: Sync](client: Client[F]) {
  import Events._

  protected val logger: LoggerTakingImplicit[FlowId] = Logger.takingImplicit[FlowId](classOf[Events[F]])

  def publish[T: Encoder](name: EventTypeName, events: List[Event[T]])(
      implicit flowId: FlowId): Kleisli[F, NakadiConfig[F], Either[String, Unit]] =
    Kleisli { config =>
      val uri         = config.baseUri / "event-types" / name.name / "events"
      val baseHeaders = List(Header("X-Flow-ID", flowId.id))

      for {
        headers <- addAuth(baseHeaders, config.oAuth2TokenProvider)
        request = Request[F](POST, uri, headers = Headers(headers)).withEntity(events)
        _       = logger.debug(request.toString)
        response <- httpClient(config, client).fetch[Either[String, Unit]](request) {
                     case Status.Successful(_) => Monad[F].pure(().asRight)
                     case Status.UnprocessableEntity(r) =>
                       r.attemptAs[List[BatchItemResponse]]
                         .fold(l => l.getLocalizedMessage, r => r.mkString("\n"))
                         .map(_.asLeft)
                     case r => r.as[String].map(_.asLeft)
                   }
      } yield response
    }
}

object Events {
  final case class BatchItemResponse(eid: Option[EventId],
                                     publishingStatus: PublishingStatus,
                                     step: Option[Step],
                                     detail: Option[String])

  object BatchItemResponse {
    implicit val batchItemResponseEncoder: Encoder[BatchItemResponse] =
      Encoder.forProduct4(
        "eid",
        "publishing_status",
        "step",
        "detail"
      )(x => BatchItemResponse.unapply(x).get)

    implicit val batchItemResponseDecoder: Decoder[BatchItemResponse] =
      Decoder.forProduct4(
        "eid",
        "publishing_status",
        "step",
        "detail"
      )(BatchItemResponse.apply)
  }

  sealed abstract class PublishingStatus(val id: String) extends EnumEntry with Product with Serializable {
    override val entryName = id
  }

  object PublishingStatus extends Enum[PublishingStatus] {
    val values = findValues
    case object Submitted extends PublishingStatus("submitted")
    case object Failed    extends PublishingStatus("failed")
    case object Aborted   extends PublishingStatus("aborted")

    implicit val eventsErrorsPublishingStatusEncoder: Encoder[PublishingStatus] =
      enumeratum.Circe.encoder(PublishingStatus)
    implicit val eventsErrorsPublishingStatusDecoder: Decoder[PublishingStatus] =
      enumeratum.Circe.decoder(PublishingStatus)
  }

  sealed abstract class Step(val id: String) extends EnumEntry with Product with Serializable {
    override val entryName = id
  }

  object Step extends Enum[Step] {
    val values = findValues
    case object None         extends Step("none")
    case object Validating   extends Step("validating")
    case object Partitioning extends Step("partitioning")
    case object Enriching    extends Step("enriching")
    case object Publishing   extends Step("publishing")

    implicit val eventsErrorsStepEncoder: Encoder[Step] =
      enumeratum.Circe.encoder(Step)
    implicit val eventsErrorsStepDecoder: Decoder[Step] =
      enumeratum.Circe.decoder(Step)
  }
}
