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

package io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign

import java.security.spec.InvalidKeySpecException
import java.security.{InvalidKeyException, SignatureException}

import io.circe.syntax._
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.JwtSignError.{
  InvalidBase64Scheme,
  InvalidKey,
  SignatureError
}
import tsec.common._
import tsec.jws.signature._
import tsec.jwt._
import tsec.signature.jca._
import zio.interop.catz._
import zio.{Task, ZIO}

trait TSecJwtSign extends JwtSign {

  override val jwtSign: JwtSign.Service[Any] = new JwtSign.Service[Any] {

    override def sign(privateKey: String, claims: Claims): ZIO[Any, JwtSignError, JwtToken] =
      normalizeKey(privateKey).b64Bytes
        .map(
          privateKey =>
            for {
              privateKey <- SHA256withRSA.buildPrivateKey[Task](privateKey).refineOrDie {
                             case _: InvalidKeyException =>
                               InvalidKey
                             case _: InvalidKeySpecException => InvalidKey
                           }
              jwtToken <- JWTSig.signToString[Task, SHA256withRSA](toTSecClaims(claims), privateKey).refineOrDie {
                           case exception: SignatureException => SignatureError(exception.getMessage)
                         }
            } yield JwtToken(jwtToken)
        )
        .getOrElse(Task.fail(InvalidBase64Scheme))
        .refineOrDie { case e: JwtSignError => e }

    private def normalizeKey(key: String) =
      key
        .replaceAll("-----BEGIN PRIVATE KEY-----", "")
        .replaceAll("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "")

    private def toTSecClaims(googleClaims: Claims): JWTClaims =
      JWTClaims(
        issuer = Some(googleClaims.issuer),
        audience = Some(JWTSingleAudience(googleClaims.audience)),
        expiration = Some(googleClaims.expiration),
        issuedAt = Some(googleClaims.issuedAt),
        customFields = Seq(("scope", googleClaims.scope.asJson)),
        subject = googleClaims.subject,
        jwtId = None
      )
  }
}

object TSecJwtSign {
  def appendable(): JwtSign = new TSecJwtSign {}
}
