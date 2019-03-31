package fs2.nakadi.client

import java.net.URI

import cats.effect.{ContextShift, IO}
import fs2.nakadi.TestResources
import fs2.nakadi.error.UnknownError
import fs2.nakadi.implicits._
import fs2.nakadi.model.{Category, EventType, EventTypeName, NakadiConfig}
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class EventClientTypeDslSpec extends FlatSpec with Matchers with TestResources {
  implicit val config: NakadiConfig[IO] = NakadiConfig(uri = new URI(""))
  implicit val cs: ContextShift[IO]     = IO.contextShift(global)

  val eventType = EventType(
    name = EventTypeName("test"),
    owningApplication = "test-app",
    category = Category.Business
  )

  "EventTypeInterpreter" should "find an existing event type" in {
    val response = new EventTypeClient[IO](client()).get(EventTypeName("test")).unsafeRunSync()

    response shouldBe Some(eventType)
  }

  it should "return None if event type doesn't exist" in {
    val response = new EventTypeClient[IO](client(found = false)).get(EventTypeName("test")).unsafeRunSync()

    response shouldBe None
  }

  it should "raise error if failed" in {
    val response = new EventTypeClient[IO](failingClient).get(eventType.name)

    assertThrows[UnknownError] {
      response.unsafeRunSync()
    }
  }

  it should "list event types" in {
    val response = new EventTypeClient[IO](client()).list.unsafeRunSync()

    response shouldBe List(eventType)
  }

  it should "delete the event type" in {
    val response = new EventTypeClient[IO](client()).delete(eventType.name)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "create an event type" in {
    val response = new EventTypeClient[IO](client()).create(eventType)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "raise error if creation failed" in {
    val response = new EventTypeClient[IO](failingClient).create(eventType)

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
