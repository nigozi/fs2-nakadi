package fs2.nakadi.api
import java.net.URI

import cats.effect.IO
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

import fs2.nakadi.error.ServerError
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
    val response = api.get(eventType.name)

    val caught = intercept[ServerError] {
      response.unsafeRunSync()
    }

    caught.status shouldBe 400
  }

  it should "list event types" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client()))
    val api      = new EventTypes(config)
    val response = api.list().unsafeRunSync()

    response shouldBe List(eventType)
  }

  it should "delete the event type" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client()))
    val api      = new EventTypes(config)
    val response = api.delete(eventType.name)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "create the event type" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(client()))
    val api      = new EventTypes(config)
    val response = api.create(eventType)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "return error if fails to create the event type" in {
    val config   = NakadiConfig(uri = new URI(""), httpClient = Some(failingClient))
    val api      = new EventTypes(config)
    val response = api.create(eventType)

    val caught = intercept[ServerError] {
      response.unsafeRunSync()
    }

    caught.status shouldBe 409
  }

  private def client(found: Boolean = true): Client[IO] = {
    val app = HttpApp[IO] {
      case _ if !found =>
        NotFound()
      case r if r.method == GET && r.uri.toString.endsWith(s"/event-types/${eventType.name.name}") =>
        Ok().map(_.withEntity(eventType))
      case r if r.method == GET && r.uri.toString.endsWith("/event-types") =>
        Ok().map(_.withEntity(List(eventType)))
      case r if r.method == DELETE =>
        Ok()
      case r if r.method == POST =>
        Ok()
    }

    Client.fromHttpApp(app)
  }

  private def failingClient: Client[IO] = {
    val app = HttpApp[IO] {
      case r if r.method == GET  => BadRequest()
      case r if r.method == POST => Conflict()
    }

    Client.fromHttpApp(app)
  }
}
