package io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator

import java.time.Instant

/**
 * Represents Authorization Server access token response.
 *
 * Access token expires in one hour and can be reused until they expire.
 *
 * @param accessToken google access token
 * @param tokenType token type
 * @param expiresAt when will token expire
 */
final case class AuthResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant
)
