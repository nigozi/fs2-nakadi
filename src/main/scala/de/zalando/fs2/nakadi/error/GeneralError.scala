package de.zalando.fs2.nakadi.error

final case class GeneralError(message: String) extends Exception {
  override def getMessage: String = s"Error from server, response is $message"
}
