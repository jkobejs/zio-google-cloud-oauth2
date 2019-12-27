package io.github.jkobejs.zio.google.cloud.oauth2.server2server.http

case class HttpAuthRequest(
  jwtToken: String,
  uri: String,
  grantType: String
)
