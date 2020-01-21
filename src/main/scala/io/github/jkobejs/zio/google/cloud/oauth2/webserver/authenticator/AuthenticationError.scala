package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http

sealed trait AuthenticationError extends RuntimeException

object AuthenticationError {
  case class HttpError(error: http.HttpError) extends AuthenticationError {
    override def getMessage: String = error.getMessage
  }
}
