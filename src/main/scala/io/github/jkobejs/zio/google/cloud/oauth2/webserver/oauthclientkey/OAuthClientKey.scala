package io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey

/**
 * Google Cloud OAuth Client ID key downloaded from  https://console.developers.google.com/apis/credentials.
 *
 * @see https://cloud.google.com/iam/docs/creating-managing-service-account-keys
 */
final case class OAuthClientKey(
  client_id: String,
  project_id: String,
  auth_uri: String,
  token_uri: String,
  auth_provider_x509_cert_url: String,
  client_secret: String,
  redirect_uris: List[String]
)

final case class Web(web: OAuthClientKey)
