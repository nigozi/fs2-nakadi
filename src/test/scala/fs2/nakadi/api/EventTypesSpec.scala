package fs2.nakadi.api
import java.net.URI

import cats.effect.IO
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

import fs2.nakadi.error.GeneralError
import fs2.nakadi.model.{Category, EventType, EventTypeName, NakadiConfig}

class EventTypesSpec extends FlatSpec with Matchers with Implicits {
  val eventType = EventType(
    name = EventTypeName("test"),
    owningApplication = "test-app",
    category = Category.Business
  )

  "EventTypes" should "find an existing event type" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client()))
    val api      = new EventTypes(config)
    val response = api.get(EventTypeName("test")).unsafeRunSync()

    response shouldBe Some(eventType)
  }

  it should "return None if event type doesn't exist" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client(found = false)))
    val api      = new EventTypes(config)
    val response = api.get(EventTypeName("test")).unsafeRunSync()

    response shouldBe None
  }

  it should "return error if call fails" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(failingClient))
    val api      = new EventTypes(config)
    val response = api.get(EventTypeName("test"))

    assertThrows[GeneralError] {
      response.unsafeRunSync()
    }
  }

  it should "list event types" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client()))
    val api      = new EventTypes(config)
    val response = api.list().unsafeRunSync()

    response shouldBe List(eventType)
  }

  private def client(found: Boolean = true): Client[IO] = {
    val app = HttpApp[IO] {
      case _ if !found =>
        NotFound()
      case r if r.method == GET && r.uri.toString.endsWith(s"/event-types/${eventType.name.name}") =>
        Ok().withEntity(eventType)
      case r if r.method == GET && r.uri.toString.endsWith("/event-types") =>
        Ok().withEntity(List(eventType))
    }

    Client.fromHttpApp(app)
  }

  private def failingClient: Client[IO] = {
    val app = HttpApp[IO](_ => BadRequest())

    Client.fromHttpApp(app)
  }
}
