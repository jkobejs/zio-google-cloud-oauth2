package io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator

/**
 * Represents config used to connect to Google OAuth 2.0 server.
 *
 * @param uri url used for creating auth requests
 * @param privateKey private key used to sign JWT token
 * @param grantType given grant
 */
final case class CloudApiConfig(
  uri: String,
  privateKey: String,
  grantType: String
)
