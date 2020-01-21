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

import zio.ZIO
import zio.macros.annotation.accessible

/**
 * `ServiceAccountKeyReader` provides api for reading `client_secret.json` file from file system.
 */
@accessible(">")
trait OAuthClientKeyReader {
  val oAuthClientKeyReader: OAuthClientKeyReader.Service[Any]
}

object OAuthClientKeyReader {
  trait Service[R] {

    /**
     * Reads Google Cloud OAuth Client ID key downloaded from  https://console.developers.google.com/apis/credentials.
     *
     * @see https://developers.google.com/identity/protocols/OAuth2WebServer
     * @param path path to `client_secret.json` file
     * @return side effect that evaluates to [[OAuthClientKey]]
     */
    def readKey(path: String): ZIO[R, OAuthClientKeyError, OAuthClientKey]
  }

}
