package io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.integration

import java.nio.file.{Files, Paths}
import java.time.Instant

import io.github.jkobejs.zio.google.cloud.oauth2.utils.{Browser, RedirectionRouter}
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator.{
  AuthApiConfig,
  AuthRequestParams,
  Authenticator
}
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey.{
  FS2OAuthClientKeyReader,
  OAuthClientKeyReader
}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import zio.blocking.Blocking
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test.{assert, suite, testM}
import zio.{Queue, Task, ZIO}
import io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.authenticator.Live

object DefaultAuthenticatorIntegrationSuite {

  val defaultAuthenticatorIntegrationSuite =
    if (Files.isRegularFile(Paths.get(io.github.jkobejs.zio.google.cloud.oauth2.BuildInfo.oauthClientKeyPath)))
      suite("Default Authenticator Integration tests")(
        testM(
          "authenticator returns access response when authentication succeeds and refreshes token when refreshToken succeeds"
        ) {
          val serviceAccountKeyReader =
            OAuthClientKeyReader.>.readKey(io.github.jkobejs.zio.google.cloud.oauth2.BuildInfo.oauthClientKeyPath)

          val managedResource = ZIO
            .runtime[Any]
            .toManaged_
            .flatMap { implicit rts =>
              val exec = rts.platform.executor.asEC
              BlazeClientBuilder[Task](exec).resource.toManaged
            }
            .map(Live.apply)

          def server(queue: Queue[String]) = ZIO.runtime[Any].flatMap { implicit rts =>
            import zio.interop.catz.implicits._
            import zio.interop.catz.taskEffectInstance
            BlazeServerBuilder[Task]
              .withHttpApp(RedirectionRouter(queue).redirectionApp)
              .resource
              .use(_ => Task.never)
          }

          for {
            serviceAccountKey <- serviceAccountKeyReader.provide(new FS2OAuthClientKeyReader with Blocking.Live {})
            authApiConfig = AuthApiConfig(
              clientId = serviceAccountKey.client_id,
              projectId = serviceAccountKey.project_id,
              authUri = serviceAccountKey.auth_uri,
              tokenUri = serviceAccountKey.token_uri,
              clientSecret = serviceAccountKey.client_secret,
              redirectUris = serviceAccountKey.redirect_uris
            )
            queue <- Queue.bounded[String](1)
            fiber <- server(queue).fork
            authUrl = Authenticator.>.createAuthUrl(
              authApiConfig,
              AuthRequestParams("https://www.googleapis.com/auth/devstorage.read_write")
            )
            _           <- Browser.open(authUrl)
            codeOrError <- queue.take
            code <- if (codeOrError == "access_denied") Task.fail(new Throwable("Error: access_denied"))
                   else ZIO.succeed(codeOrError)
            accessResponse <- Authenticator.>.authenticate(authApiConfig, code).provideManaged(managedResource)
            refreshResponse <- Authenticator.>.refreshToken(authApiConfig, accessResponse.refreshToken)
                                .provideManaged(managedResource)
            _ <- fiber.interrupt
          } yield {
            assert(accessResponse.tokenType)(equalTo("Bearer"))
            assert(accessResponse.expiresAt.getEpochSecond / 3600)(equalTo(Instant.now().getEpochSecond / 3600 + 1))
            assert(refreshResponse.tokenType)(equalTo("Bearer"))
            assert(accessResponse.expiresAt.getEpochSecond / 3600)(equalTo(Instant.now().getEpochSecond / 3600 + 1))
          }
        }
      )
    else suite("Default Authenticator Integration tests")()
}
