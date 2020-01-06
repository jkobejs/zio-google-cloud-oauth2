package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import cats.data.NonEmptyList
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http._
import zio.Managed
import zio.test.Assertion._
import zio.test.mock.Expectation
import zio.test._

object DefaultAuthenticatorSuite {

  private val cloudApiConfig = CloudApiConfig(
    "client_id",
    "project_id",
    "https://accounts.google.com/o/oauth2/auth",
    "https://oauth2.googleapis.com/token",
    "secret123",
    NonEmptyList.of("http://localhost:8081", "http://localhost:8082")
  )

  val authenticatorSuite = suite("Default Authenticator tests")(
    test("Authenticator correctly generates and escapes auth url") {

      val authRequestParams = AuthRequestParams(
        scope = "https://www.googleapis.com/auth/devstorage.read_write",
        redirect_uri = Some("http://localhost:8080")
      )

      val authUrl = Authenticator.createAuthUrl(cloudApiConfig, authRequestParams)
      assert(
        authUrl,
        equalTo(
          s"https://accounts.google.com/o/oauth2/auth?client_id=client_id&redirect_uri=http%3A%2F%2Flocalhost%3A8080&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdevstorage.read_write&access_type=offline&prompt=consent&response_type=code"
        )
      )
    },
    testM("Authenticator returns access response when authentication succeeds") {

      val httpAccessRequest = HttpAccessRequest(
        "https://oauth2.googleapis.com/token",
        HttpAccessRequestBody("code_123", "http://localhost:8081", "client_id", "secret123")
      )
      val httpAccessResponse = HttpAccessResponse("access_token_123", "Bearer", 123L, "refresh_token_123")

      val app = Authenticator.>.authenticate(cloudApiConfig, "code_123")

      val httpClientMock: Managed[Nothing, HttpClient] = HttpClient
        .authenticate(equalTo(httpAccessRequest))
        .returns(Expectation.value(httpAccessResponse))

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

      val httpRefreshRequest = HttpRefreshRequest(
        "https://oauth2.googleapis.com/token",
        HttpRefreshRequestBody("refresh_token_123", "client_id", "secret123")
      )
      val httpRefreshResponse = HttpRefreshResponse("access_token_234", "Bearer", 123L)

      val app = Authenticator.>.refreshToken(cloudApiConfig, "refresh_token_123")

      val httpClientMock: Managed[Nothing, HttpClient] =
        HttpClient
          .refreshToken(equalTo(httpRefreshRequest))
          .returns(Expectation.value(httpRefreshResponse))

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

      val httpRefreshRequest = HttpRefreshRequest(
        "https://oauth2.googleapis.com/token",
        HttpRefreshRequestBody("refresh_token_123", "client_id", "secret123")
      )

      val app = Authenticator.>.refreshToken(cloudApiConfig, "refresh_token_123")

      val httpClientMock: Managed[Nothing, HttpClient] =
        HttpClient
          .refreshToken(equalTo(httpRefreshRequest))
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
