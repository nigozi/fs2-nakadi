package fs2.nakadi.dsl

import java.net.URI

import cats.effect.IO
import fs2.nakadi.Implicits
import fs2.nakadi.dsl.Registries._
import fs2.nakadi.model.EnrichmentStrategy.MetadataEnrichment
import fs2.nakadi.model.NakadiConfig
import fs2.nakadi.model.PartitionStrategy.{Hash, Random, UserDefined}
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalatest.{FlatSpec, Matchers}

class RegistriesSpec extends FlatSpec with Matchers with Implicits {
  private implicit val config: NakadiConfig = NakadiConfig(uri = new URI(""), httpClient = Some(client()))

  "Registries" should "return enrichment strategies" in {
    val response = enrichmentStrategies.foldMap(compiler).unsafeRunSync()

    response shouldBe List(MetadataEnrichment)
  }

  it should "return partition strategies" in {
    val response = partitionStrategies.foldMap(compiler).unsafeRunSync()

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
