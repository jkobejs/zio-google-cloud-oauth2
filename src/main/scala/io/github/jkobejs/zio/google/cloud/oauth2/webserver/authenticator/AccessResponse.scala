package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import java.time.Instant

/**
 * Represents Authorization Server authentication response, having both access and refresh token.
 * Access token expires in one hour and can be reused until it expires.
 *
 * @param accessToken google access token
 * @param tokenType token type
 * @param expiresAt when will token expire
 * @param refreshToken google refresh token
 */
final case class AccessResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant,
  refreshToken: String
)
