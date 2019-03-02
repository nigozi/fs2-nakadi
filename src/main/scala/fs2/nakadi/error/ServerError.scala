package fs2.nakadi.error

final case class ServerError(status: Int, body: String) extends Exception {
  override def getMessage: String = s"Error from server, status: $status, response: $body"
}