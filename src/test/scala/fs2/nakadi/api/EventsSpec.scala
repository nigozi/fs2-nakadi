package fs2.nakadi.api
import java.net.URI
import java.util.UUID

import cats.effect.IO
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

import fs2.nakadi.error.{BatchItemResponse, EventValidation, PublishingStatus, Step}
import fs2.nakadi.model.{EventId, EventTypeName, Metadata, NakadiConfig}
import fs2.nakadi.model.Event.Business
import io.circe.syntax._

class EventsSpec extends FlatSpec with Matchers with Implicits {
  private val validationError = BatchItemResponse(
    eid = Some(EventId(UUID.randomUUID().toString)),
    publishingStatus = PublishingStatus.Failed,
    step = Some(Step.Publishing),
    detail = None
  )

  private val event = Business("""{"foo": "bar"}""".asJson, Metadata())

  "Events" should "publish events" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client()))
    val api      = new Events(config)
    val response = api.publish(EventTypeName("test"), List(event))

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "return error when publish fails" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client(success = false)))
    val api      = new Events(config)
    val response = api.publish(EventTypeName("test"), List(event))

    val caught = intercept[EventValidation] {
      response.unsafeRunSync()
    }

    caught.batchItemResponse shouldBe List(validationError)
  }

  private def client(success: Boolean = true): Client[IO] = {
    val app = HttpApp[IO] {
      case r if r.method == POST && success =>
        Ok()
      case r if r.method == POST && !success =>
        UnprocessableEntity().map(_.withEntity(List(validationError)))
    }

    Client.fromHttpApp(app)
  }
}
