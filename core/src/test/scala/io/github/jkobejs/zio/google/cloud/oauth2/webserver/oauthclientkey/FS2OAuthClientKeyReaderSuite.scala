package io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey

import cats.data.NonEmptyList
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey.OAuthClientKeyError.{
  FileDoesNotExist,
  InvalidJsonFormat,
  InvalidPathError
}
import zio.blocking.Blocking
import zio.test.Assertion.equalTo
import zio.test.{assert, suite, testM}

object FS2OAuthClientKeyReaderSuite {
  val fs2OAuthClientKeyReaderSuite = suite("FS2 OAuth Client Key Reader tests")(
    testM("fs2 OAuth client key reader reads OAuth client key file from path") {
      val app = OAuthClientKeyReader.>.readKey("core/src/test/resources/client_secret.json")

      for {
        oAuthClientKey <- app.provide(new FS2OAuthClientKeyReader with Blocking.Live {})
      } yield assert(
        oAuthClientKey,
        equalTo(
          OAuthClientKey(
            client_id = "client_id.apps.googleusercontent.com",
            project_id = "project_id_1",
            auth_uri = "https://accounts.google.com/o/oauth2/auth",
            token_uri = "https://oauth2.googleapis.com/token",
            auth_provider_x509_cert_url = "https://www.googleapis.com/oauth2/v1/certs",
            client_secret = "client_secret",
            redirect_uris = NonEmptyList.of("http://localhost:8080")
          )
        )
      )
    },
    testM("fs2 OAuth client key reader fails when file doesn't exist") {
      val nonExistingPath = "core/src/test/resources/non-existing-client_secret.json"
      val app             = OAuthClientKeyReader.>.readKey(nonExistingPath)

      for {
        oAuthClientKeyEither <- app.provide(new FS2OAuthClientKeyReader with Blocking.Live {}).either
      } yield {
        assert(oAuthClientKeyEither, equalTo(Left(FileDoesNotExist(nonExistingPath))))
      }
    },
    testM("fs2 OAuth client key reader fails when path is invalid") {
      val invalidPath = "core/src/test/resources/\u0000"
      val app         = OAuthClientKeyReader.>.readKey(invalidPath)

      for {
        oAuthClientKeyEither <- app.provide(new FS2OAuthClientKeyReader with Blocking.Live {}).either
      } yield {
        assert(oAuthClientKeyEither, equalTo(Left(InvalidPathError(invalidPath))))
      }
    },
    testM("fs2 OAuth client key reader fails for invalid json") {
      val invalidJsonPath = "core/src/test/resources/invalid-json-client_secret.json"
      val app             = OAuthClientKeyReader.>.readKey(invalidJsonPath)

      for {
        oAuthClientKeyEither <- app.provide(new FS2OAuthClientKeyReader with Blocking.Live {}).either
      } yield {
        assert(oAuthClientKeyEither, equalTo(Left(InvalidJsonFormat(invalidJsonPath))))
      }
    }
  )
}
