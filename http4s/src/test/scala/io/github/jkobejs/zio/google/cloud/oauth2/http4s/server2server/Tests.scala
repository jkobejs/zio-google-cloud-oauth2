package io.github.jkobejs.zio.google.cloud.oauth2.server2server

import io.github.jkobejs.zio.google.cloud.oauth2.http4s.server2server.http.Http4sClientSuite
import io.github.jkobejs.zio.google.cloud.oauth2.http4s.server2server.integration.DefaultAuthenticatorIntegrationSuite
import zio.test._

object Tests
    extends DefaultRunnableSpec(
      suite("All Google Cloud Auth http4s server2server suites")(
        Http4sClientSuite.http4sClientSuite,
        DefaultAuthenticatorIntegrationSuite.defaultAuthenticatorIntegrationSuite
      )
    )
