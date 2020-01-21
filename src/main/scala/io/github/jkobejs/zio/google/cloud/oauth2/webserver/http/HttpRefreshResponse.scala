package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http

final case class HttpRefreshResponse(
  access_token: String,
  token_type: String,
  expires_in: Long
)
