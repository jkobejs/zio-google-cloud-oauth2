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
