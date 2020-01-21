package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http

final case class HttpAccessRequestBody(
  code: String,
  redirect_uri: String,
  client_id: String,
  client_secret: String,
  grant_type: String = "authorization_code"
)
