package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.common.urlencoding.UrlEncodedWriter

final case class AuthRequest(
  client_id: String,
  redirect_uri: String,
  scope: String,
  access_type: String = "offline",
  state: Option[String] = None,
  include_granted_scopes: Option[Boolean] = None,
  login_hint: Option[String] = None,
  prompt: Option[String] = Some("consent"),
  response_type: String = "code"
)

object AuthRequest {
  implicit val urlEncodedWriterAuthRequest: UrlEncodedWriter[AuthRequest] = UrlEncodedWriter.gen
}
