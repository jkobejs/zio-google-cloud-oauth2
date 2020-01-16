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

package io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey

import zio.ZIO
import zio.macros.annotation.accessible

/**
 * `ServiceAccountKeyReader` provides api for reading service account key file from file system.
 */
@accessible(">")
trait ServiceAccountKeyReader {
  val serviceAccountKeyReader: ServiceAccountKeyReader.Service[Any]
}

object ServiceAccountKeyReader {
  trait Service[R] {

    /**
     * Reads Google Cloud service account key created using the GCP Console or the gcloud command-line tool.
     *
     * @see https://cloud.google.com/iam/docs/creating-managing-service-account-keys
     *
     * @param path path to service account key file
     *
     * @return side effect that evaluates to [[ServiceAccountKey]]
     */
    def readKey(path: String): ZIO[R, ServiceAccountKeyError, ServiceAccountKey]
  }
}
