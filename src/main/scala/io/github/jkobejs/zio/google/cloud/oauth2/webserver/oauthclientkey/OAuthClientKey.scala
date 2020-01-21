package io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey

import cats.data.NonEmptyList

/**
 * Google Cloud OAuth Client ID key downloaded from  https://console.developers.google.com/apis/credentials.
 */
final case class OAuthClientKey(
  client_id: String,
  project_id: String,
  auth_uri: String,
  token_uri: String,
  auth_provider_x509_cert_url: String,
  client_secret: String,
  redirect_uris: NonEmptyList[String]
)

final case class Web(web: OAuthClientKey)
