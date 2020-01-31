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

package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import java.time.Instant
import java.util.concurrent.TimeUnit

import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http._
import zio.ZIO
import zio.clock.Clock
import zio.macros.annotation.accessible

/**
 * `Authenticator` provides api for performing Google Web server OAuth 2.0 flow.
 * Underneath it communicates to google auth server using HTTP.
 *
 * @see https://developers.google.com/identity/protocols/OAuth2WebServer
 */
@accessible(">")
trait Authenticator {
  val authenticator: Authenticator.Service[Any]
}

object Authenticator {

  trait Service[R] {

    /**
     * Creates authorization url that should be used to redirect Google user to consent prompt
     *
     * @param authApiConfig    [[AuthApiConfig]] - Google Cloud client app configuration
     * @param authRequestParams [[AuthRequestParams]] - Authorization request parameters. Most of the time, you'd only want to set `scope`, leaving the rest of arguments default. If `redirect_uri` is not provided, it will be set to a first element from [[AuthApiConfig]].redirect_uris list
     * @return authorization url
     */
    final def createAuthUrl(authApiConfig: AuthApiConfig, authRequestParams: AuthRequestParams): String = {
      import io.github.jkobejs.zio.google.cloud.oauth2.common.urlencoding.UrlEncodedWriter.ops._

      val authReqQueryParamsEncoded = AuthRequest(
        authApiConfig.clientId,
        authRequestParams.redirectUri.getOrElse(authApiConfig.redirectUris.head),
        authRequestParams.scope,
        authRequestParams.accessType,
        authRequestParams.state,
        authRequestParams.includeGrantedScopes,
        authRequestParams.loginHint,
        authRequestParams.prompt,
        authRequestParams.responseType
      ).toUrlEncoded

      s"${authApiConfig.authUri}?$authReqQueryParamsEncoded"
    }

    /**
     * Performs authentication on server. As per google cloud documentation access token will be valid for next hour
     *
     * @param authApiConfig [[AuthApiConfig]]
     * @param authorizationCode authorization code obtained by user consent from redirection uri
     * @return [[AccessResponse]]
     */
    def authenticate(
      authApiConfig: AuthApiConfig,
      authorizationCode: String
    ): ZIO[R, AuthenticationError, AccessResponse]

    /**
     * Performs access token refresh. As per google cloud documentation access token will be valid for next hour
     *
     * @param authApiConfig
     * @param refreshToken refresh token used for obtaining new access token. As per google cloud documentation refresh token never expires.
     * @return [[RefreshResponse]]
     */
    def refreshToken(
      authApiConfig: AuthApiConfig,
      refreshToken: String
    ): ZIO[R, AuthenticationError, RefreshResponse]
  }

  trait Default extends Authenticator {

    val clock: Clock.Service[Any]
    val httpClient: HttpClient.Service[Any]

    override val authenticator: Service[Any] = new Service[Any] {

      override def authenticate(
        authApiConfig: AuthApiConfig,
        authorizationCode: String
      ): ZIO[Any, AuthenticationError, AccessResponse] =
        (for {
          response <- httpClient
                       .authenticate(
                         HttpAccessRequest(
                           authApiConfig.tokenUri,
                           HttpAccessRequestBody(
                             authorizationCode,
                             authApiConfig.redirectUris.head,
                             authApiConfig.clientId,
                             authApiConfig.clientSecret
                           )
                         )
                       )
          currentTimestamp <- clock.currentTime(TimeUnit.SECONDS)
        } yield AccessResponse(
          response.access_token,
          response.token_type,
          Instant.ofEpochSecond(currentTimestamp + response.expires_in),
          response.refresh_token
        )).refineOrDie {
          case e: HttpError => AuthenticationError.HttpError(e)
        }

      override def refreshToken(
        authApiConfig: AuthApiConfig,
        refreshToken: String
      ): ZIO[Any, AuthenticationError, RefreshResponse] =
        (for {
          response <- httpClient
                       .refreshToken(
                         HttpRefreshRequest(
                           authApiConfig.tokenUri,
                           HttpRefreshRequestBody(refreshToken, authApiConfig.clientId, authApiConfig.clientSecret)
                         )
                       )
          currentTimestamp <- clock.currentTime(TimeUnit.SECONDS)
        } yield RefreshResponse(
          response.access_token,
          response.token_type,
          Instant.ofEpochSecond(currentTimestamp + response.expires_in)
        )).refineOrDie {
          case e: HttpError => AuthenticationError.HttpError(e)
        }
    }
  }
}
