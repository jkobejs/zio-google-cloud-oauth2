package io.github.jkobejs.zio.google.cloud.oauth2

import io.github.jkobejs.zio.google.cloud.oauth2.integration.DefaultAuthenticatorIntegrationSuite
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.DefaultAuthenticatorSuite
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.http.Http4sClientSuite
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey.FS2ServiceAccountKeyReaderSuite
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.TSecJwtSignSuite
import zio.test._

object Tests
    extends DefaultRunnableSpec(
      suite("All Google Cloud Auth suites")(
        DefaultAuthenticatorSuite.authenticatorSuite,
        TSecJwtSignSuite.tsecJwtSignSuite,
        FS2ServiceAccountKeyReaderSuite.fs2ServiceAccountKeyReaderSuite,
        Http4sClientSuite.http4sClientSuite,
        DefaultAuthenticatorIntegrationSuite.defaultAuthenticatorIntegrationSuite
      )
    )
