package io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign

sealed trait JwtSignError extends RuntimeException

object JwtSignError {
  final case object InvalidBase64Scheme extends JwtSignError {
    override def getMessage: String = "Invalid Base64 key scheme"
  }
  final case object InvalidKey extends JwtSignError { override def getMessage: String = "Invalid key" }

  final case class SignatureError(message: String) extends JwtSignError {
    override def getMessage: String = message
  }
}
