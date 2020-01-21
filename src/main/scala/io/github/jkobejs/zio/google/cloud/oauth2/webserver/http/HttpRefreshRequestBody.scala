package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http

final case class HttpRefreshRequestBody(
  refresh_token: String,
  client_id: String,
  client_secret: String,
  grant_type: String = "refresh_token"
)
