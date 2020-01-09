package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import java.time.Instant

/**
 * Response from refresh token request.
 *
 * @param accessToken google access token
 * @param tokenType token type
 * @param expiresAt when will token expire
 */
final case class RefreshResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant
)
