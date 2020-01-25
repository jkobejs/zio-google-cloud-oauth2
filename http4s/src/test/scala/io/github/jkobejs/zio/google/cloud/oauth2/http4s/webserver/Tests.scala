package io.github.jkobejs.zio.google.cloud.oauth2.webserver

import io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.http.Http4sClientSuite
import io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.integration.DefaultAuthenticatorIntegrationSuite
import zio.test.{suite, DefaultRunnableSpec}

object Tests
    extends DefaultRunnableSpec(
      suite("All Google Cloud OAuth http4s Web Server suites")(
        Http4sClientSuite.http4sClientSuite,
        DefaultAuthenticatorIntegrationSuite.defaultAuthenticatorIntegrationSuite
      )
    )
