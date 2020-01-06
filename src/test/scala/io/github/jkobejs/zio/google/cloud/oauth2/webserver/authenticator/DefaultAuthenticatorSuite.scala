package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.webserver.Fixtures
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http._
import zio.Managed
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation

object DefaultAuthenticatorSuite {

  val authenticatorSuite = suite("Default Authenticator tests")(
    test("Authenticator correctly generates and escapes auth url") {

      val authUrl = Authenticator.createAuthUrl(Fixtures.cloudApiConfig, Fixtures.authRequestParams)
      assert(
        authUrl,
        equalTo(
          s"https://accounts.google.com/o/oauth2/auth?client_id=client_id&redirect_uri=http%3A%2F%2Flocalhost%3A8080&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdevstorage.read_write&access_type=offline&prompt=consent&response_type=code"
        )
      )
    },
    testM("Authenticator returns access response when authentication succeeds") {

      val app = Authenticator.>.authenticate(Fixtures.cloudApiConfig, "code_123")

      val httpClientMock: Managed[Nothing, HttpClient] = HttpClient
        .authenticate(equalTo(Fixtures.httpAccessRequest))
        .returns(Expectation.value(Fixtures.httpAccessResponse))

      val mockEnv = httpClientMock.map { httpC =>
        new Authenticator.Default {
          override val httpClient = httpC.httpClient
        }
      }

      for {
        result <- app.provideManaged(mockEnv)
      } yield assert(
        result,
        equalTo(
          AccessResponse("access_token_123", "Bearer", 123L, "refresh_token_123")
        )
      )
    },
    testM("Authenticator returns refresh token response when token refreshing succeeds") {

      val app = Authenticator.>.refreshToken(Fixtures.cloudApiConfig, "refresh_token_123")

      val httpClientMock: Managed[Nothing, HttpClient] =
        HttpClient
          .refreshToken(equalTo(Fixtures.httpRefreshRequest))
          .returns(Expectation.value(Fixtures.httpRefreshResponse))

      val mockEnv = httpClientMock.map { httpC =>
        new Authenticator.Default {
          override val httpClient = httpC.httpClient
        }
      }

      for {
        result <- app.provideManaged(mockEnv)
      } yield assert(
        result,
        equalTo(
          RefreshResponse("access_token_234", "Bearer", 123L)
        )
      )
    },
    testM("Authenticator returns http error when http service fails") {

      val app = Authenticator.>.refreshToken(Fixtures.cloudApiConfig, "refresh_token_123")

      val httpClientMock: Managed[Nothing, HttpClient] =
        HttpClient
          .refreshToken(equalTo(Fixtures.httpRefreshRequest))
          .returns(Expectation.failure(HttpError.ResponseParseError("error")))

      val mockEnv = httpClientMock.map { httpC =>
        new Authenticator.Default {
          override val httpClient = httpC.httpClient
        }
      }
      assertM(
        app.provideManaged(mockEnv).either,
        equalTo(Left(AuthenticationError.HttpError(HttpError.ResponseParseError("error"))))
      )
    }
  )
}
