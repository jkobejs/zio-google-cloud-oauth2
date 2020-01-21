package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.common.urlencoding.UrlEncodedWriter

/**
 * Authentication request used for obtaining user consent. It is used to generate authentication url that user should be redirected to.
 *
 * @param client_id The client ID for your application. You can find this value in the API Console
 * @param redirect_uri  Determines where the API server redirects the user after the user completes the authorization flow
 * @param scope A space-delimited list of scopes that identify the resources that your application could access on the user's behalf
 * @param access_type Indicates whether your application can refresh access tokens when the user is not present at the browser (online or offline)
 * @param state Specifies any string value that your application uses to maintain state between your authorization request and the authorization server's response. The server returns the exact value that you send as a name=value pair in the hash (#) fragment of the redirect_uri after the user consents to or denies your application's access request
 * @param include_granted_scopes Enables applications to use incremental authorization to request access to additional scopes in context
 * @param login_hint The server uses the hint to simplify the login flow either by prefilling the email field in the sign-in form or by selecting the appropriate multi-login session
 * @param prompt A space-delimited, case-sensitive list of prompts to present the user. If you don't specify this parameter, the user will be prompted only the first time your app requests access. Possible values are: none, consent, select_account
 * @param response_type Response type - should be set to "code" for this type of requests
 */
final private[authenticator] case class AuthRequest(
  client_id: String,
  redirect_uri: String,
  scope: String,
  access_type: String = "offline",
  state: Option[String] = None,
  include_granted_scopes: Option[Boolean] = None,
  login_hint: Option[String] = None,
  prompt: Option[String] = Some("consent"),
  response_type: String = "code"
)

object AuthRequest {
  implicit val urlEncodedWriterAuthRequest: UrlEncodedWriter[AuthRequest] = UrlEncodedWriter.gen
}
