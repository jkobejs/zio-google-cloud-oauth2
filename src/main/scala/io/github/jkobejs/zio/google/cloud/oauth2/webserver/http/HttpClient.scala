package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http
import zio.ZIO
import zio.macros.annotation.{accessible, mockable}

/***
 * Provides api for making auth http requests.
 */
@accessible(">")
@mockable
trait HttpClient {
  val httpClient: HttpClient.Service[Any]
}

object HttpClient {
  trait Service[R] {

    /**
     * Makes http authentication request
     *
     * @param request [[HttpAccessRequest]]
     * @return [[HttpAccessResponse]]
     */
    def authenticate(
      request: HttpAccessRequest
    ): ZIO[R, HttpError, HttpAccessResponse]

    /**
     * Makes http refresh token request
     *
     * @param request [[HttpRefreshRequest]]
     * @return [[HttpRefreshResponse]]
     */
    def refreshToken(
      request: HttpRefreshRequest
    ): ZIO[R, HttpError, HttpRefreshResponse]
  }
}
