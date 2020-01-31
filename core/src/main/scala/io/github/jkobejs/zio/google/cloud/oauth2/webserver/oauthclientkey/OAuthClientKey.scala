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
