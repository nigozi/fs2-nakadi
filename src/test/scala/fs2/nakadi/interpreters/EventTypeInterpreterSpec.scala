package fs2.nakadi.interpreters

import java.net.URI

import cats.effect.IO
import fs2.nakadi.TestResources
import fs2.nakadi.error.UnknownError
import fs2.nakadi.implicits._
import fs2.nakadi.instances.ContextShifts
import fs2.nakadi.model.{Category, EventType, EventTypeName, NakadiConfig}
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

class EventTypeInterpreterSpec extends FlatSpec with Matchers with ContextShifts with TestResources {
  implicit val config: NakadiConfig[IO] = NakadiConfig(uri = new URI(""))

  val eventType = EventType(
    name = EventTypeName("test"),
    owningApplication = "test-app",
    category = Category.Business
  )

  "EventTypeInterpreter" should "find an existing event type" in {
    val response = new EventTypeInterpreter[IO](client()).get(EventTypeName("test")).unsafeRunSync()

    response shouldBe Some(eventType)
  }

  it should "return None if event type doesn't exist" in {
    val response = new EventTypeInterpreter[IO](client(found = false)).get(EventTypeName("test")).unsafeRunSync()

    response shouldBe None
  }

  it should "raise error if failed" in {
    val response = new EventTypeInterpreter[IO](failingClient).get(eventType.name)

    assertThrows[UnknownError] {
      response.unsafeRunSync()
    }
  }

  it should "list event types" in {
    val response = new EventTypeInterpreter[IO](client()).list.unsafeRunSync()

    response shouldBe List(eventType)
  }

  it should "delete the event type" in {
    val response = new EventTypeInterpreter[IO](client()).delete(eventType.name)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "create an event type" in {
    val response = new EventTypeInterpreter[IO](client()).create(eventType)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "raise error if creation failed" in {
    val response = new EventTypeInterpreter[IO](failingClient).create(eventType)

    assertThrows[UnknownError] {
      response.unsafeRunSync()
    }
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
