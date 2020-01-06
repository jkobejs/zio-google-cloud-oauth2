package io.github.jkobejs.zio.google.cloud.oauth2.webserver

import io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator.DefaultAuthenticatorSuite
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http.Http4sClientSuite
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey.FS2OAuthClientKeyReaderSuite
import zio.test.{suite, DefaultRunnableSpec}

object Tests
    extends DefaultRunnableSpec(
      suite("All Google Cloud OAuth Web Server suites")(
        FS2OAuthClientKeyReaderSuite.fs2OAuthClientKeyReaderSuite,
        DefaultAuthenticatorSuite.authenticatorSuite,
        Http4sClientSuite.http4sClientSuite
      )
    )
