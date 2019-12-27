package io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator

import java.util.concurrent.TimeUnit

import zio.duration.Duration

/**
 * Represents the JWT Claims used in Google server-to-server oauth
 *
 *
 * @param issuer Issuer claim, Case insensitive
 * @param scope A space-delimited list of the permissions that the application requests
 * @param audience The audience Case-sensitive. Can be either a list or a single string
 * @param subject Subject, Case-sensitive string when defined
 * @param expiresIn Controls when auth token will expire (Google API default is 1 hour)
 */
final case class CloudApiClaims(
  issuer: String,
  scope: String,
  audience: String,
  subject: Option[String] = None,
  expiresIn: Duration = Duration(1, TimeUnit.HOURS)
)
