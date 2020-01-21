package io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator

import java.time.Instant
import java.util.concurrent.TimeUnit

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.AuthenticatorError.{HttpError, SignError}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.http.{HttpAuthRequest, HttpAuthResponse, HttpClient}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.JwtSignError.InvalidKey
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.{Claims, JwtSign, JwtSignError, JwtToken}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.{authenticator, http}
import zio.duration.Duration
import zio.test.Assertion.equalTo
import zio.test.mock.{Expectation, MockClock}
import zio.test.{assert, assertM, suite, testM}

object DefaultAuthenticatorSuite {
  val authenticatorSuite = suite("Default Authenticator tests")(
    testM("authenticator returns auth response when authorization succeeds") {
      val currentTime = 1000L
      val expiresIn   = 10L

      val (cloudApiClaims, cloudApiConfig, claims, jwtToken, httpAuthResponse) = generateData(currentTime, expiresIn)

      val clockMock = MockClock.currentTime(equalTo(TimeUnit.SECONDS)).returns(Expectation.value(currentTime))

      val jwtSignMock = (
        JwtSign.sign(equalTo((cloudApiConfig.privateKey, claims))).returns(Expectation.value(jwtToken))
      )

      val httpClientMock = (
        HttpClient
          .auth(equalTo(HttpAuthRequest("token", cloudApiConfig.uri, cloudApiConfig.grantType)))
          .returns(Expectation.value(httpAuthResponse))
        )

      val combinedEnv = (clockMock &&& httpClientMock &&& jwtSignMock).map {
        case ((testC, httpC), jwtS) =>
          new Authenticator.Default {
            val clock      = testC.clock
            val jwtSign    = jwtS.jwtSign
            val httpClient = httpC.httpClient
          }
      }

      val app = Authenticator.>.auth(cloudApiConfig, cloudApiClaims)

      for {
        result <- app.provideManaged(combinedEnv)
      } yield {
        assert(
          result,
          equalTo(
            authenticator.AuthResponse(
              accessToken = httpAuthResponse.access_token,
              tokenType = httpAuthResponse.token_type,
              expiresAt = Instant.ofEpochSecond(currentTime + cloudApiClaims.expiresIn.asScala.toSeconds)
            )
          )
        )
      }
    },
    testM("authenticator returns http error when http service fails") {
      val currentTime = 1000L
      val exipresIn   = 10L

      val (cloudApiClaims, cloudApiConfig, claims, jwtToken, _) = generateData(currentTime, exipresIn)

      val clockMock = MockClock.currentTime(equalTo(TimeUnit.SECONDS)).returns(Expectation.value(currentTime))

      val jwtSignMock = (
        JwtSign.sign(equalTo((cloudApiConfig.privateKey, claims))).returns(Expectation.value(jwtToken))
      )

      val httpClientMock = (
        HttpClient
          .auth(equalTo(HttpAuthRequest("token", cloudApiConfig.uri, cloudApiConfig.grantType)))
          .returns(Expectation.failure(http.HttpError.ResponseParseError("error")))
        )

      val combinedEnv = (clockMock &&& httpClientMock &&& jwtSignMock).map {
        case ((testC, httpC), jwtS) =>
          new Authenticator.Default {
            val clock      = testC.clock
            val jwtSign    = jwtS.jwtSign
            val httpClient = httpC.httpClient
          }
      }

      val app = Authenticator.>.auth(cloudApiConfig, cloudApiClaims)

      assertM(
        app.provideManaged(combinedEnv).either,
        equalTo(Left(HttpError(http.HttpError.ResponseParseError("error"))))
      )
    },
    testM("authenticator returns sign error when signer service fails") {
      val currentTime = 1000L
      val exipresIn   = 10L

      val (cloudApiClaims, cloudApiConfig, claims, _, httpAuthResponse) = generateData(currentTime, exipresIn)

      val clockMock = MockClock.currentTime(equalTo(TimeUnit.SECONDS)).returns(Expectation.value(currentTime))

      val jwtSignMock = (
        JwtSign.sign(equalTo((cloudApiConfig.privateKey, claims))).returns(Expectation.failure(JwtSignError.InvalidKey))
      )

      val httpClientMock = (
        HttpClient
          .auth(equalTo(HttpAuthRequest("token", cloudApiConfig.uri, cloudApiConfig.grantType)))
          .returns(Expectation.value(httpAuthResponse))
        )

      val combinedEnv = (clockMock &&& httpClientMock &&& jwtSignMock).map {
        case ((testC, httpC), jwtS) =>
          new Authenticator.Default {
            val clock      = testC.clock
            val jwtSign    = jwtS.jwtSign
            val httpClient = httpC.httpClient
          }
      }

      val app = Authenticator.>.auth(cloudApiConfig, cloudApiClaims)

      assertM(app.provideManaged(combinedEnv).either, equalTo(Left(SignError(InvalidKey))))
    }
  )

  private def generateData(currentTime: Long, expiresIn: Long) = {
    val cloudApiClaims = AuthApiClaims(
      issuer = "clientEmail",
      scope = "scope",
      audience = "url",
      expiresIn = Duration(expiresIn, TimeUnit.SECONDS)
    )
    val cloudApiConfig = AuthApiConfig(
      uri = "http://localhost:8000/oauth2/v4/token",
      privateKey = "privateKey",
      grantType = "grant"
    )

    val claims = Claims(
      issuer = "clientEmail",
      scope = "scope",
      audience = "url",
      issuedAt = Instant.ofEpochSecond(currentTime),
      expiration = Instant.ofEpochSecond(currentTime + expiresIn)
    )

    val jwtToken = JwtToken(token = "token")
    val httpAuthResponse = HttpAuthResponse(
      access_token = "accessToken",
      token_type = "JWT",
      expires_in = expiresIn
    )

    (cloudApiClaims, cloudApiConfig, claims, jwtToken, httpAuthResponse)
  }
}
