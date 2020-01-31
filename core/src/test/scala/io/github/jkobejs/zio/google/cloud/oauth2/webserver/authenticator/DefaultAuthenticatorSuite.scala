package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import java.time.Instant
import java.util.concurrent.TimeUnit

import io.github.jkobejs.zio.google.cloud.oauth2.webserver.Fixtures
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http._
import zio.Managed
import zio.test.Assertion._
import zio.test._
import zio.test.mock.{Expectation, MockClock}

object DefaultAuthenticatorSuite {

  val authenticatorSuite = suite("Default Authenticator tests")(
    test("Authenticator correctly generates and escapes auth url") {

      val authUrl = Authenticator.>.createAuthUrl(Fixtures.authApiConfig, Fixtures.authRequestParams)
      assert(
        authUrl,
        equalTo(
          s"https://accounts.google.com/o/oauth2/auth?client_id=clientId&redirect_uri=http%3A%2F%2Flocalhost%3A8080&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdevstorage.read_write&access_type=offline&prompt=consent&response_type=code"
        )
      )
    },
    testM("Authenticator returns access response when authentication succeeds") {

      val app = Authenticator.>.authenticate(Fixtures.authApiConfig, "code_123")

      val clockMock = MockClock.currentTime(equalTo(TimeUnit.SECONDS)).returns(Expectation.value(Fixtures.currentTime))

      val httpClientMock: Managed[Nothing, HttpClient] = HttpClient
        .authenticate(equalTo(Fixtures.httpAccessRequest))
        .returns(Expectation.value(Fixtures.httpAccessResponse))

      val combinedEnv = (clockMock &&& httpClientMock).map {
        case (testC, httpC) =>
          new Authenticator.Default {
            override val clock      = testC.clock
            override val httpClient = httpC.httpClient
          }
      }

      for {
        result <- app.provideManaged(combinedEnv)
      } yield assert(
        result,
        equalTo(
          AccessResponse(
            "access_token_123",
            "Bearer",
            Instant.ofEpochSecond(Fixtures.currentTime + Fixtures.httpAccessResponse.expires_in),
            "refresh_token_123"
          )
        )
      )
    },
    testM("Authenticator returns refresh token response when token refreshing succeeds") {

      val app = Authenticator.>.refreshToken(Fixtures.authApiConfig, "refresh_token_123")

      val clockMock = MockClock.currentTime(equalTo(TimeUnit.SECONDS)).returns(Expectation.value(Fixtures.currentTime))

      val httpClientMock: Managed[Nothing, HttpClient] =
        HttpClient
          .refreshToken(equalTo(Fixtures.httpRefreshRequest))
          .returns(Expectation.value(Fixtures.httpRefreshResponse))

      val combinedEnv = (clockMock &&& httpClientMock).map {
        case (testC, httpC) =>
          new Authenticator.Default {
            override val clock      = testC.clock
            override val httpClient = httpC.httpClient
          }
      }

      for {
        result <- app.provideManaged(combinedEnv)
      } yield assert(
        result,
        equalTo(
          RefreshResponse(
            "access_token_234",
            "Bearer",
            Instant.ofEpochSecond(Fixtures.currentTime + Fixtures.httpRefreshResponse.expires_in)
          )
        )
      )
    },
    testM("Authenticator returns http error when http service fails") {

      val app = Authenticator.>.refreshToken(Fixtures.authApiConfig, "refresh_token_123")

      val clockMock = MockClock.currentTime(equalTo(TimeUnit.SECONDS)).returns(Expectation.value(Fixtures.currentTime))

      val httpClientMock: Managed[Nothing, HttpClient] =
        HttpClient
          .refreshToken(equalTo(Fixtures.httpRefreshRequest))
          .returns(Expectation.failure(HttpError.ResponseParseError("error")))

      val combinedEnv = (clockMock &&& httpClientMock).map {
        case (testC, httpC) =>
          new Authenticator.Default {
            override val clock      = testC.clock
            override val httpClient = httpC.httpClient
          }
      }

      assertM(
        app.provideManaged(combinedEnv).either,
        equalTo(Left(AuthenticationError.HttpError(HttpError.ResponseParseError("error"))))
      )
    }
  )
}
