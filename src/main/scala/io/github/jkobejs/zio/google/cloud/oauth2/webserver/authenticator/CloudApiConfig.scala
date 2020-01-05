package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import cats.data.NonEmptyList

/**
 * Represents config used to connect to Google OAuth 2.0 server.
 *
 * @param client_id Google OAuth 2.0 Client ID
 * @param project_id Google project ID
 * @param auth_uri url used for creating authorization requests (obtaining authorization code)
 * @param token_uri url used for creating authentication requests (obtaining access and refresh tokens)
 * @param client_secret Google client password
 * @param redirect_uris List of redirect URIs, must be verified in Google Console
 */
final case class CloudApiConfig(
  client_id: String,
  project_id: String,
  auth_uri: String,
  token_uri: String,
  client_secret: String,
  redirect_uris: NonEmptyList[String]
)
