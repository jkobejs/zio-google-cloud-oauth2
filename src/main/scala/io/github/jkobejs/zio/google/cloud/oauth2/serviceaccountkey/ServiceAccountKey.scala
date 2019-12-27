package io.github.jkobejs.zio.google.cloud.oauth2.serviceaccountkey

/**
 * Google Cloud service account key created using the GCP Console or the gcloud command-line tool.
 *
 * @see https://cloud.google.com/iam/docs/creating-managing-service-account-keys
 */
case class ServiceAccountKey(
  `type`: String,
  project_id: String,
  private_key_id: String,
  private_key: String,
  client_email: String,
  client_id: String,
  auth_uri: String,
  token_uri: String,
  auth_provider_x509_cert_url: String,
  client_x509_cert_url: String
)
