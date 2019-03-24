package fs2.nakadi.error
import fs2.nakadi.implicits._
import io.circe.syntax._
import io.circe.{Json, JsonObject, Printer}

final case class Problem(`type`: Option[String] = None,
                         title: Option[String] = None,
                         status: Option[Int] = None,
                         detail: Option[String] = None,
                         instance: Option[String] = None,
                         extraFields: Option[JsonObject] = None) {

  override def toString: String =
    Printer.noSpaces
      .copy(dropNullValues = true)
      .pretty(toJson(this))

  private def toJson(p: Problem): Json = p.asJson
}

final case class BasicServerError(error: String, errorDescription: String)

final case class GeneralError(problem: Problem) extends Exception {
  override def getMessage: String = s"Error from server, response is $problem"
}

final case class OtherError(error: BasicServerError) extends Exception {
  override def getMessage: String = s"Error from server, response is $error"
}

final case class ExpectedHeader(headerName: String) extends Exception {
  override def getMessage: String =
    s"Expected header '$headerName' is missing in the response"
}

final case class UnknownError(details: String) extends Exception {
  override def getMessage: String = s"Unexpected error: $details"
}
