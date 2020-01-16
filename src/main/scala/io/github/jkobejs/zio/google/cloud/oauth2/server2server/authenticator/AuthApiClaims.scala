/*
 * Copyright 2019 Josip Grgurica
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
final case class AuthApiClaims(
  issuer: String,
  scope: String,
  audience: String,
  subject: Option[String] = None,
  expiresIn: Duration = Duration(1, TimeUnit.HOURS)
)
