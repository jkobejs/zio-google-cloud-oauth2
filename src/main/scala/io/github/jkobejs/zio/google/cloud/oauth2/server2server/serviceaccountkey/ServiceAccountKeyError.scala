package io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey

sealed trait ServiceAccountKeyError extends RuntimeException

object ServiceAccountKeyError {
  case class InvalidPathError(path: String)  extends ServiceAccountKeyError
  case class FileDoesNotExist(path: String)  extends ServiceAccountKeyError
  case class InvalidJsonFormat(path: String) extends ServiceAccountKeyError
}
