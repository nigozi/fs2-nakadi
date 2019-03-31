package fs2.nakadi.client

import java.net.URI

import cats.effect.{ContextShift, IO}
import fs2.nakadi.TestResources
import fs2.nakadi.model.EnrichmentStrategy.MetadataEnrichment
import fs2.nakadi.model.NakadiConfig
import fs2.nakadi.model.PartitionStrategy.{Hash, Random, UserDefined}
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class RegistryClientSpec extends FlatSpec with Matchers with TestResources {
  private implicit val config: NakadiConfig[IO] = NakadiConfig(uri = new URI(""))
  implicit val cs: ContextShift[IO]             = IO.contextShift(global)

  private val interpreter = new RegistryClient[IO](client())

  "RegistryInterpreter" should "list enrichment strategies" in {
    val response = interpreter.enrichmentStrategies.unsafeRunSync()

    response shouldBe List(MetadataEnrichment)
  }

  it should "list partition strategies" in {
    val response = interpreter.partitionStrategies.unsafeRunSync()

    response shouldBe List(Random, UserDefined, Hash)
  }

  private def client(): Client[IO] = {
    val app = HttpApp[IO] {
      case r if r.method == GET && r.uri.toString.endsWith("/registry/enrichment-strategies") =>
        Ok().map(_.withEntity(List("metadata_enrichment")))
      case r if r.method == GET && r.uri.toString.endsWith("/registry/partition-strategies") =>
        Ok().map(_.withEntity(List("random", "user_defined", "hash")))
    }

    Client.fromHttpApp(app)
  }
}
