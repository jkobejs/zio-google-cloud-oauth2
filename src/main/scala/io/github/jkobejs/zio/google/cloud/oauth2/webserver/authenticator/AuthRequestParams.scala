package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

final case class AuthRequestParams(
  scope: String,
  redirect_uri: Option[String] = None,
  access_type: String = "offline",
  state: Option[String] = None,
  include_granted_scopes: Option[Boolean] = None,
  login_hint: Option[String] = None,
  prompt: Option[String] = Some("consent"),
  response_type: String = "code"
)
