package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

final case class AccessResponse(
  access_token: String,
  token_type: String,
  expires_in: Long,
  refresh_token: String
)
