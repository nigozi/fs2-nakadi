package fs2.nakadi
import cats.syntax.either._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Encoder, Json, Printer}

package object interpreters {
  private val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  def encode[T: Encoder](entity: T): Json =
    parse(printer.pretty(entity.asJson)).valueOr(e => sys.error(s"failed to encode the entity: ${e.message}"))
}
