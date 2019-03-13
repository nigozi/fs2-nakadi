package fs2.nakadi.interpreters

import java.net.URI
import java.util.UUID

import cats.effect.IO
import fs2.nakadi.error.{BatchItemResponse, EventValidation, PublishingStatus, Step}
import fs2.nakadi.model.Event.Business
import fs2.nakadi.model.{EventId, EventTypeName, Metadata, NakadiConfig}
import fs2.nakadi.TestResources
import fs2.nakadi.dsl.Implicits
import io.circe.Json
import io.circe.syntax._
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

class EventInterpreterSpec extends FlatSpec with Matchers with Implicits with TestResources {
  implicit val config: NakadiConfig[IO] = NakadiConfig(uri = new URI(""))

  private val validationError = BatchItemResponse(
    eid = Some(EventId(UUID.randomUUID().toString)),
    publishingStatus = PublishingStatus.Failed,
    step = Some(Step.Publishing),
    detail = None
  )

  private val event = Business("""{"foo": "bar"}""".asJson, Metadata())

  "EventInterpreter" should "publish events" in {
    val response = new EventInterpreter[IO](client()).publish[Json](EventTypeName("test"), List(event))

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "return error when publish fails" in {
    val response = new EventInterpreter[IO](client(success = false)).publish[Json](EventTypeName("test"), List(event))

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
