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
