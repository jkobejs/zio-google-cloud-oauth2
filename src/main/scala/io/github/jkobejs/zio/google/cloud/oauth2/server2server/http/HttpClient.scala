package io.github.jkobejs.zio.google.cloud.oauth2.server2server.http

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
     * Makes http auth request.
     *
     * @param request [[HttpAuthRequest]]
     * @return effect that, if evaluated, will return [[HttpAuthResponse]]
     */
    def auth(request: HttpAuthRequest): ZIO[R, HttpError, HttpAuthResponse]
  }
}
