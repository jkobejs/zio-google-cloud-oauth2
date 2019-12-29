package io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator

import java.time.Instant
import java.util.concurrent.TimeUnit

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.AuthenticatorError.SignError
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.http
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.http.{Http4sClient, HttpAuthRequest, HttpClient}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.{Claims, JwtSign, JwtSignError, TSecJwtSign}
import zio._
import zio.clock.Clock
import zio.macros.annotation.accessible

/**
 * `Authenticator` provides api for performing Google server-to-server OAuth 2.0 flow.
 * Underneath it communicates to google auth server using HTTP.
 *
 * @see https://developers.google.com/identity/protocols/OAuth2ServiceAccount
 */
@accessible(">")
trait Authenticator {
  val authenticator: Authenticator.Service[Any]
}

object Authenticator {
  trait Service[R] {

    /**
     * Performs authorization on server. As per google cloud documentation access token will be valid for next hour.
     *
     * @param cloudApiConfig [[CloudApiConfig]]
     * @param cloudApiClaims [[CloudApiClaims]]
     *
     * @return effect that, if evaluated, will return the [[AuthResponse]]
     */
    def auth(cloudApiConfig: CloudApiConfig, cloudApiClaims: CloudApiClaims): ZIO[R, AuthenticatorError, AuthResponse]
  }

  trait Default extends Authenticator {
    self =>
    val jwtSign: JwtSign.Service[Any]
    val httpClient: HttpClient.Service[Any]
    val clock: Clock.Service[Any]

    override val authenticator: Service[Any] =
      new Service[Any] {
        override def auth(
          cloudApiConfig: CloudApiConfig,
          cloudApiClaims: CloudApiClaims
        ): ZIO[Any, AuthenticatorError, AuthResponse] =
          (for {
            currentTimestamp <- clock.currentTime(TimeUnit.SECONDS)
            response         <- request(cloudApiConfig, cloudApiClaims, currentTimestamp)
          } yield AuthResponse(
            accessToken = response.access_token,
            tokenType = response.token_type,
            expiresAt = Instant.ofEpochSecond(currentTimestamp + response.expires_in)
          )).refineOrDie {
            case e: http.HttpError => AuthenticatorError.HttpError(e)
            case e: JwtSignError   => SignError(e)
          }

        private def request(
          cloudApiConfig: CloudApiConfig,
          claims: CloudApiClaims,
          currentTimestamp: Long
        ): Task[http.HttpAuthResponse] =
          for {
            jwtToken <- jwtSign.sign(cloudApiConfig.privateKey, newClaims(claims, currentTimestamp))
            response <- httpClient
                         .auth(
                           HttpAuthRequest(
                             jwtToken = jwtToken.token,
                             uri = cloudApiConfig.uri,
                             grantType = cloudApiConfig.grantType
                           )
                         )
          } yield response

        private def newClaims(claims: CloudApiClaims, currentTimestamp: Long): Claims =
          Claims(
            issuer = claims.issuer,
            scope = claims.scope,
            audience = claims.audience,
            subject = claims.subject,
            issuedAt = Instant.ofEpochSecond(currentTimestamp),
            expiration = Instant.ofEpochSecond(currentTimestamp).plusMillis(claims.expiresIn.toMillis)
          )
      }
  }

  trait Live extends Default with TSecJwtSign with Http4sClient with Clock.Live
}
