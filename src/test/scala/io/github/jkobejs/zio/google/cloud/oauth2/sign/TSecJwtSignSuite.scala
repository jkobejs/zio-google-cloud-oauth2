package io.github.jkobejs.zio.google.cloud.oauth2.sign

import java.time.Instant

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.JwtSignError.{InvalidBase64Scheme, InvalidKey}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.{Claims, JwtToken, TSecJwtSign}
import zio.test.Assertion.equalTo
import zio.test.{assertM, suite, testM}

object TSecJwtSignSuite {
  val tsecJwtSignSuite = suite("Default TSec Jwt Sign tests")(
    testM("jwt signer returns signed token for valid private key") {
      val tsecJwtSign = new TSecJwtSign {}

      val claims = Claims(
        issuer = "clientEmail",
        scope = "scope",
        audience = "url",
        expiration = Instant.ofEpochSecond(100).plusSeconds(3600),
        issuedAt = Instant.ofEpochSecond(100)
      )

      val privateKey =
        "MIIBOgIBAAJBAJHPYfmEpShPxAGP12oyPg0CiL1zmd2V84K5dgzhR9TFpkAp2kl2" +
          "9BTc8jbAY0dQW4Zux+hyKxd6uANBKHOWacUCAwEAAQJAQVyXbMS7TGDFWnXieKZh" +
          "Dm/uYA6sEJqheB4u/wMVshjcQdHbi6Rr0kv7dCLbJz2v9bVmFu5i8aFnJy1MJOpA" +
          "2QIhAPyEAaVfDqJGjVfryZDCaxrsREmdKDlmIppFy78/d8DHAiEAk9JyTHcapckD" +
          "uSyaE6EaqKKfyRwSfUGO1VJXmPjPDRMCIF9N900SDnTiye/4FxBiwIfdynw6K3dW" +
          "fBLb6uVYr/r7AiBUu/p26IMm6y4uNGnxvJSqe+X6AxR6Jl043OWHs4AEbwIhANuz" +
          "Ay3MKOeoVbx0L+ruVRY5fkW+oLHbMGtQ9dZq7Dp9"

      assertM(
        tsecJwtSign.jwtSign.sign(privateKey, claims),
        equalTo(
          JwtToken(
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJjbGllbnRFbWFpbCIsImF1ZCI6InVybCIsImV4cCI6MzcwMCwiaWF0IjoxMDAsInNjb3BlIjoic2NvcGUifQ.HgTCMC9a5uASY7qguE24C0_t4iGUrAI00bq9Aj7CR9k5cUsKQNXOSPGivkp62WbSSGjOU9yTu1FtZ5Ne2TAtyw"
          )
        )
      )
    },
    testM("jwt signer fails for key with invalid base64 scheme") {
      val tsecJwtSign = new TSecJwtSign {}

      val claims = Claims(
        issuer = "clientEmail",
        scope = "scope",
        audience = "url",
        expiration = Instant.ofEpochSecond(100).plusSeconds(3600),
        issuedAt = Instant.ofEpochSecond(100)
      )

      val privateKey = "MIIBOgIBAAJBAJHPYfmEpShPxAGP12oyPg0CiL1zmd2V84K5dgzhR9TFpkAp2kl2y"

      assertM(
        tsecJwtSign.jwtSign.sign(privateKey, claims).either,
        equalTo(Left(InvalidBase64Scheme))
      )
    },
    testM("jwt signer fails for key with invalid base64 scheme") {
      val tsecJwtSign = new TSecJwtSign {}

      val claims = Claims(
        issuer = "clientEmail",
        scope = "scope",
        audience = "url",
        expiration = Instant.ofEpochSecond(100).plusSeconds(3600),
        issuedAt = Instant.ofEpochSecond(100)
      )

      val privateKey = "Invalid Key"

      assertM(
        tsecJwtSign.jwtSign.sign(privateKey, claims).either,
        equalTo(Left(InvalidKey))
      )
    }
  )
}
