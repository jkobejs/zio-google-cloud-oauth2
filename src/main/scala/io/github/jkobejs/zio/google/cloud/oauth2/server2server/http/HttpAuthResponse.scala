package io.github.jkobejs.zio.google.cloud.oauth2.server2server.http

final case class HttpAuthResponse(
  access_token: String,
  token_type: String,
  expires_in: Long
)
