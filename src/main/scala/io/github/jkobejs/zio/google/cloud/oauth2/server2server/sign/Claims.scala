package io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign

import java.time.Instant

final case class Claims(
  issuer: String,
  scope: String,
  audience: String,
  expiration: Instant,
  issuedAt: Instant,
  subject: Option[String] = None
)
