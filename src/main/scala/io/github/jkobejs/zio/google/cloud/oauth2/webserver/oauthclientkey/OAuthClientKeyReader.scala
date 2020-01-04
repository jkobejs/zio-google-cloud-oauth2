package io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey

import zio.ZIO
import zio.macros.annotation.accessible

/**
 * `ServiceAccountKeyReader` provides api for reading `client_secret.json` file from file system.
 */
@accessible(">")
trait OAuthClientKeyReader {
  val oAuthClientKeyReader: OAuthClientKeyReader.Service[Any]
}

object OAuthClientKeyReader {
  trait Service[R] {

    /**
     * Reads Google Cloud OAuth Client ID key downloaded from  https://console.developers.google.com/apis/credentials.
     *
     * @see https://developers.google.com/identity/protocols/OAuth2WebServer
     * @param path path to `client_secret.json` file
     * @return side effect that evaluates to [[OAuthClientKey]]
     */
    def readKey(path: String): ZIO[R, OAuthClientKeyError, OAuthClientKey]
  }

}
