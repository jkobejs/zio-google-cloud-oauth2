/*
 * Copyright 2019 Josip Grgurica and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

/**
 * Authentication request parameters. Used to construct [[AuthRequest]]
 *
 * @param scope A space-delimited list of scopes that identify the resources that your application could access on the user's behalf
 * @param redirectUri  Determines where the API server redirects the user after the user completes the authorization flow
 * @param accessType Indicates whether your application can refresh access tokens when the user is not present at the browser (online or offline)
 * @param state Specifies any string value that your application uses to maintain state between your authorization request and the authorization server's response. The server returns the exact value that you send as a name=value pair in the hash (#) fragment of the redirect_uri after the user consents to or denies your application's access request
 * @param includeGrantedScopes Enables applications to use incremental authorization to request access to additional scopes in context
 * @param loginHint The server uses the hint to simplify the login flow either by prefilling the email field in the sign-in form or by selecting the appropriate multi-login session
 * @param prompt A space-delimited, case-sensitive list of prompts to present the user. If you don't specify this parameter, the user will be prompted only the first time your app requests access. Possible values are: none, consent, select_account
 * @param responseType Response type - should be set to "code" for this type of requests
 */
final case class AuthRequestParams(
  scope: String,
  redirectUri: Option[String] = None,
  accessType: String = "offline",
  state: Option[String] = None,
  includeGrantedScopes: Option[Boolean] = None,
  loginHint: Option[String] = None,
  prompt: Option[String] = Some("consent"),
  responseType: String = "code"
)
