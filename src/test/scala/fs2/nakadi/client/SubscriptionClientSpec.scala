package fs2.nakadi.client

import java.net.URI
import java.util.UUID

import cats.effect.{ContextShift, IO}
import fs2.nakadi.TestResources
import fs2.nakadi.error.UnknownError
import fs2.nakadi.implicits._
import fs2.nakadi.model._
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.OptionValues._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionClientSpec extends FlatSpec with Matchers with TestResources {
  implicit val config: NakadiConfig[IO] = NakadiConfig(uri = new URI(""))
  implicit val cs: ContextShift[IO]             = IO.contextShift(global)

  private val subscription = Subscription(owningApplication = "test", id = Some(SubscriptionId(UUID.randomUUID())))

  "SubscriptionInterpreter" should "find an existing subscription" in {
    val response = new SubscriptionClient[IO](client()).get(subscription.id.value).unsafeRunSync()

    response shouldBe Some(subscription)
  }

  it should "return None if subscription doesn't exist" in {
    val response = new SubscriptionClient[IO](client(found = false)).get(subscription.id.value).unsafeRunSync()

    response shouldBe None
  }

  it should "raise error if call fails" in {
    val response = new SubscriptionClient[IO](failingClient).get(subscription.id.value)

    assertThrows[UnknownError] {
      response.unsafeRunSync()
    }
  }

  it should "list subscriptions" in {
    val response = new SubscriptionClient[IO](client()).list().unsafeRunSync()

    response.items shouldBe List(subscription)
  }

  it should "delete the event type" in {
    val response = new SubscriptionClient[IO](client()).delete(subscription.id.value)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "create an event type" in {
    val response = new SubscriptionClient[IO](client()).create(subscription)

    noException should be thrownBy response.unsafeRunSync()
  }

  it should "raise error if creation failed" in {
    val response = new SubscriptionClient[IO](failingClient).create(subscription)

    assertThrows[UnknownError] {
      response.unsafeRunSync()
    }
  }

  private def client(found: Boolean = true): Client[IO] = {
    val app = HttpApp[IO] {
      case _ if !found =>
        NotFound()
      case r if r.method == GET && r.uri.toString.endsWith(s"/subscriptions/${subscription.id.value.id.toString}") =>
        Ok().map(_.withEntity(subscription))
      case r if r.method == GET && r.uri.toString.endsWith("/subscriptions") =>
        Ok().map(
          _.withEntity(
            SubscriptionQuery(links = PaginationLinks(prev = None, next = None), items = List(subscription))))
      case r if r.method == DELETE =>
        Ok()
      case r if r.method == POST && r.uri.toString.endsWith("/subscriptions") =>
        Ok().map(_.withEntity(subscription))
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
