package io.github.jkobejs.zio.google.cloud.oauth2.integration

import java.nio.file.{Files, Paths}
import java.time.Instant

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.Authenticator.Live
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.{
  Authenticator,
  CloudApiClaims,
  CloudApiConfig
}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey.{FS2ServiceAccountKeyReader, ServiceAccountKeyReader}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.{Task, ZIO}
import zio.blocking.Blocking
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test.{assert, suite, testM}

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
            .map(
              client4s =>
                new Live {
                  val client: Client[zio.Task] = client4s
                }
            )

          for {
            serviceAccountKey <- serviceAccountKeyReader.provide(new FS2ServiceAccountKeyReader with Blocking.Live {})
            cloudApiConfig = CloudApiConfig(
              uri = serviceAccountKey.token_uri,
              privateKey = serviceAccountKey.private_key,
              grantType = "urn:ietf:params:oauth:grant-type:jwt-bearer"
            )
            cloudApiClaims = CloudApiClaims(
              issuer = serviceAccountKey.client_email,
              scope = "https://www.googleapis.com/auth/devstorage.read_write",
              audience = serviceAccountKey.token_uri
            )
            authResponse <- Authenticator.>.auth(cloudApiConfig, cloudApiClaims).provideManaged(managedResource)
          } yield {
            assert(authResponse.tokenType, equalTo("Bearer"))
            assert(authResponse.expiresAt.getEpochSecond() / 3600, equalTo(Instant.now().getEpochSecond() / 3600 + 1))
          }
        }
      )
    else suite("Default Authenticator Integration tests")()
}
