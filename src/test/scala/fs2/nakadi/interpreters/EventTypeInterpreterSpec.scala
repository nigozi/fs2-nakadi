package fs2.nakadi.interpreters

import java.net.URI

import cats.effect.IO
import fs2.nakadi.error.ServerError
import fs2.nakadi.model.{Category, EventType, EventTypeName, NakadiConfig}
import fs2.nakadi.{Implicits, TestResources}
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

class EventTypeInterpreterSpec extends FlatSpec with Matchers with Implicits with TestResources {
  implicit val config: NakadiConfig[IO] = NakadiConfig(uri = new URI(""))

  val eventType = EventType(
    name = EventTypeName("test"),
    owningApplication = "test-app",
    category = Category.Business
  )

  "EventTypes" should "find an existing event type" in {
    val response = new EventTypeInterpreter[IO](client()).get(EventTypeName("test")).unsafeRunSync()

    response shouldBe Some(eventType)
  }

  it should "return None if event type doesn't exist" in {
    val response = new EventTypeInterpreter[IO](client(found = false)).get(EventTypeName("test")).unsafeRunSync()

    response shouldBe None
  }

  it should "return error if call fails" in {
    val response = new EventTypeInterpreter[IO](failingClient).get(eventType.name)

    val caught = intercept[ServerError] {
      response.unsafeRunSync()
    }

    caught.status shouldBe 400
  }

  it should "list event types" in {
    val response = new EventTypeInterpreter[IO](client()).list.unsafeRunSync()

    response shouldBe List(eventType)
  }

  it should "delete the event type" in {
    val response = new EventTypeInterpreter[IO](client()).delete(eventType.name)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "create the event type" in {
    val response = new EventTypeInterpreter[IO](client()).create(eventType)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "return error if fails to create the event type" in {
    val response = new EventTypeInterpreter[IO](failingClient).create(eventType)

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
