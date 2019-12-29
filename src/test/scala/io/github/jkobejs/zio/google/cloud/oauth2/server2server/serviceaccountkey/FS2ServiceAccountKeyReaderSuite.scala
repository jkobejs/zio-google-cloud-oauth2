package io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey

import zio.test.Assertion.equalTo
import zio.test.{assert, suite, testM}
import zio.blocking.Blocking
import ServiceAccountKeyError.FileDoesNotExist
import ServiceAccountKeyError.InvalidPathError
import ServiceAccountKeyError.InvalidJsonFormat

object FS2ServiceAccountKeyReaderSuite {
  val fs2ServiceAccountKeyReaderSuite = suite("FS2 Service Account Key Reader tests")(
    testM("fs2 service account key reader reads service account key file from path") {
      val app = ServiceAccountKeyReader.>.readKey("src/test/resources/service-account.json")

      for {
        serviceAccountKey <- app.provide(new FS2ServiceAccountKeyReader with Blocking.Live {})
      } yield assert(
        serviceAccountKey,
        equalTo(
          ServiceAccountKey(
            `type` = "service_account",
            project_id = "projectId",
            private_key_id = "privateKeyId",
            private_key = "privateKey",
            client_email = "clientEmail",
            client_id = "clientId",
            auth_uri = "https://accounts.google.com/o/oauth2/auth",
            token_uri = "https://oauth2.googleapis.com/token",
            auth_provider_x509_cert_url = "https://www.googleapis.com/oauth2/v1/certs",
            client_x509_cert_url =
              "https://www.googleapis.com/robot/v1/metadata/x509/[client_email].iam.gserviceaccount.com"
          )
        )
      )
    },
    testM("fs2 service account key reader fails when file doesn't exist") {
      val nonExistingPath = "src/test/resources/non-existing-service-account.json"
      val app             = ServiceAccountKeyReader.>.readKey(nonExistingPath)

      for {
        serviceAccountKeyEither <- app.provide(new FS2ServiceAccountKeyReader with Blocking.Live {}).either
      } yield {
        assert(serviceAccountKeyEither, equalTo(Left(FileDoesNotExist(nonExistingPath))))
      }
    },
    testM("fs2 service account key reader fails when path is invalid") {
      val invalidPath = "src/test/resources/\u0000"
      val app         = ServiceAccountKeyReader.>.readKey(invalidPath)

      for {
        serviceAccountKeyEither <- app.provide(new FS2ServiceAccountKeyReader with Blocking.Live {}).either
      } yield {
        assert(serviceAccountKeyEither, equalTo(Left(InvalidPathError(invalidPath))))
      }
    },
    testM("fs2 service account key reader fails for invalid json") {
      val invalidJsonPath = "src/test/resources/invalid-json-service-account.json"
      val app             = ServiceAccountKeyReader.>.readKey(invalidJsonPath)

      for {
        serviceAccountKeyEither <- app.provide(new FS2ServiceAccountKeyReader with Blocking.Live {}).either
      } yield {
        assert(serviceAccountKeyEither, equalTo(Left(InvalidJsonFormat(invalidJsonPath))))
      }
    }
  )
}
