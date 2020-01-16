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

package io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign

import zio.ZIO
import zio.macros.annotation.{accessible, mockable}

/***
 * Provides api for signing private keys and creating jwt token.
 */
@accessible
@mockable
trait JwtSign {
  val jwtSign: JwtSign.Service[Any]
}

object JwtSign {
  trait Service[R] {

    /**
     * Sign and create jwt token
     *
     * @param privateKey [[String]]
     * @param claims [[Claims]]
     * @return effect that, if evaluated, will return [[JwtToken]]
     */
    def sign(privateKey: String, claims: Claims): ZIO[R, JwtSignError, JwtToken]
  }
}
