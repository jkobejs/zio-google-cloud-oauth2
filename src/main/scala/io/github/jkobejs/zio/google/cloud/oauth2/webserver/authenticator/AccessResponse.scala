package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import java.time.Instant

final case class AccessResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant,
  refreshToken: String
)
