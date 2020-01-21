package io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey

sealed trait OAuthClientKeyError extends RuntimeException

object OAuthClientKeyError {
  case class InvalidPathError(path: String)  extends OAuthClientKeyError
  case class FileDoesNotExist(path: String)  extends OAuthClientKeyError
  case class InvalidJsonFormat(path: String) extends OAuthClientKeyError
}
