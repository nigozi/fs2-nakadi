package fs2.nakadi.error
import io.circe.JsonObject

final case class Problem(`type`: Option[String],
                   title: String,
                   status: Option[Int] = None,
                   detail: Option[String] = None,
                   instance: Option[String] = None,
                   extraFields: JsonObject = JsonObject.empty)

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
