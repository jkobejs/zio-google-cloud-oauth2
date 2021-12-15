package io.github.jkobejs.zio.google.cloud.oauth2.http4s.server2server.integration

import java.nio.file.{Files, Paths}
import java.time.Instant

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.{
  AuthApiClaims,
  AuthApiConfig,
  Authenticator
}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey.{
  FS2ServiceAccountKeyReader,
  ServiceAccountKeyReader
}
import org.http4s.client.blaze.BlazeClientBuilder
import zio.blocking.Blocking
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test.{assert, suite, testM}
import zio.{Task, ZIO}
import io.github.jkobejs.zio.google.cloud.oauth2.http4s.server2server.authenticator.Live

object DefaultAuthenticatorIntegrationSuite {
  val defaultAuthenticatorIntegrationSuite =
    if (Files.isRegularFile(Paths.get(io.github.jkobejs.zio.google.cloud.oauth2.BuildInfo.serviceAccountKeyPath)))
      suite("Default Authenticator Integration tests")(
        testM("authenticator returns auth response when authorization succeeds") {
          val serviceAccountKeyReader =
            ServiceAccountKeyReader.>.readKey(io.github.jkobejs.zio.google.cloud.oauth2.BuildInfo.serviceAccountKeyPath)

          val managedResource = ZIO
            .runtime[Any]
            .toManaged_
            .flatMap { implicit rts =>
              val exec = rts.platform.executor.asEC
              BlazeClientBuilder[Task](exec).resource.toManaged
            }
            .map(Live.apply)

          for {
            serviceAccountKey <- serviceAccountKeyReader.provide(new FS2ServiceAccountKeyReader with Blocking.Live {})
            cloudApiConfig = AuthApiConfig(
              uri = serviceAccountKey.token_uri,
              privateKey = serviceAccountKey.private_key,
              grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer"
            )
            cloudApiClaims = AuthApiClaims(
              issuer = serviceAccountKey.client_email,
              scope = "https://www.googleapis.com/auth/devstorage.read_write",
              audience = serviceAccountKey.token_uri
            )
            authResponse <- Authenticator.>.auth(cloudApiConfig, cloudApiClaims).provideManaged(managedResource)
          } yield {
            assert(authResponse.tokenType)(equalTo("Bearer"))
            assert(authResponse.expiresAt.getEpochSecond() / 3600)(equalTo(Instant.now().getEpochSecond() / 3600 + 1))
          }
        }
      )
    else suite("Default Authenticator Integration tests")()
}
