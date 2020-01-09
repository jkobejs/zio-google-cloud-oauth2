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

  /**
   * Creates authorization url that should be used to redirect Google user to consent prompt
   *
   * @param cloudApiConfig [[CloudApiConfig]] - Google Cloud client app configuration
   * @param authRequestParams [[AuthRequestParams]] - Authorization request parameters. Most of the time, you'd only want to set `scope`, leaving the rest of arguments default. If `redirect_uri` is not provided, it will be set to a first element from [[CloudApiConfig]].redirect_uris list
   * @return authorization url
   */
  def createAuthUrl(cloudApiConfig: CloudApiConfig, authRequestParams: AuthRequestParams): String = {
    import io.github.jkobejs.zio.google.cloud.oauth2.common.urlencoding.UrlEncodedWriter.ops._

    val authReqQueryParamsEncoded = AuthRequest(
      cloudApiConfig.client_id,
      authRequestParams.redirect_uri.getOrElse(cloudApiConfig.redirect_uris.head),
      authRequestParams.scope,
      authRequestParams.access_type,
      authRequestParams.state,
      authRequestParams.include_granted_scopes,
      authRequestParams.login_hint,
      authRequestParams.prompt,
      authRequestParams.response_type
    ).toUrlEncoded

    s"${cloudApiConfig.auth_uri}?$authReqQueryParamsEncoded"
  }

  trait Service[R] {

    /**
     * Performs authentication on server. As per google cloud documentation access token will be valid for next hour
     *
     * @param cloudApiConfig [[CloudApiConfig]]
     * @param authorizationCode authorization code obtained by user consent from redirection uri
     * @return [[AccessResponse]]
     */
    def authenticate(
      cloudApiConfig: CloudApiConfig,
      authorizationCode: String
    ): ZIO[R, AuthenticationError, AccessResponse]

    /**
     * Performs access token refresh. As per google cloud documentation access token will be valid for next hour
     *
     * @param cloudApiConfig
     * @param refreshToken refresh token used for obtaining new access token. As per google cloud documentation refresh token never expires.
     * @return [[RefreshResponse]]
     */
    def refreshToken(
      cloudApiConfig: CloudApiConfig,
      refreshToken: String
    ): ZIO[R, AuthenticationError, RefreshResponse]
  }

  trait Default extends Authenticator {

    val clock: Clock.Service[Any]
    val httpClient: HttpClient.Service[Any]

    override val authenticator: Service[Any] = new Service[Any] {

      override def authenticate(
        cloudApiConfig: CloudApiConfig,
        authorizationCode: String
      ): ZIO[Any, AuthenticationError, AccessResponse] =
        (for {
          response <- httpClient
                       .authenticate(
                         HttpAccessRequest(
                           cloudApiConfig.token_uri,
                           HttpAccessRequestBody(
                             authorizationCode,
                             cloudApiConfig.redirect_uris.head,
                             cloudApiConfig.client_id,
                             cloudApiConfig.client_secret
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
        cloudApiConfig: CloudApiConfig,
        refreshToken: String
      ): ZIO[Any, AuthenticationError, RefreshResponse] =
        (for {
          response <- httpClient
                       .refreshToken(
                         HttpRefreshRequest(
                           cloudApiConfig.token_uri,
                           HttpRefreshRequestBody(refreshToken, cloudApiConfig.client_id, cloudApiConfig.client_secret)
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

  trait Live extends Default with Http4sClient with Clock.Live
}
