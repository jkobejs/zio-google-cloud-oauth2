package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import cats.data.NonEmptyList

/**
 * Represents config used to connect to Google OAuth 2.0 server.
 *
 * @param clientId Google OAuth 2.0 Client ID
 * @param projectId Google project ID
 * @param authUri url used for creating authorization requests (obtaining authorization code)
 * @param tokenUri url used for creating authentication requests (obtaining access and refresh tokens)
 * @param clientSecret Google client password
 * @param redirectUris List of redirect URIs, must be verified in Google Console
 */
final case class AuthApiConfig(
  clientId: String,
  projectId: String,
  authUri: String,
  tokenUri: String,
  clientSecret: String,
  redirectUris: NonEmptyList[String]
)
