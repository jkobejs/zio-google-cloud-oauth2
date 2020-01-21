package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http

sealed trait HttpError extends RuntimeException

object HttpError {
  final case class ResponseParseError(message: String) extends HttpError {
    override def getMessage: String = message
  }

  final case class HttpRequestError(status: String, body: String) extends HttpError {
    override def getMessage: String =
      s"""
         | - status: $status
         | - body: $body
    """.stripMargin
  }

  final case class UriParseError(message: String) extends HttpError {
    override def getMessage: String = message
  }
}
