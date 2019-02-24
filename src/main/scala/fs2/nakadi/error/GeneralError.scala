package fs2.nakadi.error

final case class GeneralError(message: String) extends Exception {
  override def getMessage: String = message
}
