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
