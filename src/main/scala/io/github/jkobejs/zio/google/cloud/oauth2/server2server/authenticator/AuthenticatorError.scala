package io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.http
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.JwtSignError

sealed trait AuthenticatorError extends RuntimeException

object AuthenticatorError {
  case class HttpError(error: http.HttpError) extends AuthenticatorError {
    override def getMessage: String = error.getMessage
  }
  case class SignError(error: JwtSignError) extends AuthenticatorError {
    override def getMessage: String = error.getMessage
  }
}
