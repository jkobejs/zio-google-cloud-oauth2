package io.github.jkobejs.zio.google.cloud.oauth2.webserver

import cats.data.NonEmptyList
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator.{AuthApiConfig, AuthRequestParams}
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http._

object Fixtures {

  val currentTime = 1000L

  val authApiConfig: AuthApiConfig = AuthApiConfig(
    "clientId",
    "projectId",
    "https://accounts.google.com/o/oauth2/auth",
    "https://oauth2.googleapis.com/token",
    "secret123",
    NonEmptyList.of("http://localhost:8081", "http://localhost:8082")
  )

  val authRequestParams: AuthRequestParams = AuthRequestParams(
    scope = "https://www.googleapis.com/auth/devstorage.read_write",
    redirectUri = Some("http://localhost:8080")
  )

  val httpAccessRequest: HttpAccessRequest = HttpAccessRequest(
    "https://oauth2.googleapis.com/token",
    HttpAccessRequestBody("code_123", "http://localhost:8081", "clientId", "secret123")
  )
  val httpAccessResponse: HttpAccessResponse =
    HttpAccessResponse("access_token_123", "Bearer", 123L, "refresh_token_123")

  val httpRefreshRequest: HttpRefreshRequest = HttpRefreshRequest(
    "https://oauth2.googleapis.com/token",
    HttpRefreshRequestBody("refresh_token_123", "clientId", "secret123")
  )
  val httpRefreshResponse: HttpRefreshResponse = HttpRefreshResponse("access_token_234", "Bearer", 123L)

}
