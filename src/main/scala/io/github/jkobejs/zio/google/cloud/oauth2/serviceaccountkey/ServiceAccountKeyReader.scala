package io.github.jkobejs.zio.google.cloud.oauth2.serviceaccountkey

import zio.ZIO
import zio.macros.annotation.accessible

/**
 * `ServiceAccountKeyReader` provides api for reading service account key file from file system.
 */
@accessible(">")
trait ServiceAccountKeyReader {
  val serviceAccountKeyReader: ServiceAccountKeyReader.Service[Any]
}

object ServiceAccountKeyReader {
  trait Service[R] {

    /**
     * Reads Google Cloud service account key created using the GCP Console or the gcloud command-line tool.
     *
     * @see https://cloud.google.com/iam/docs/creating-managing-service-account-keys
     *
     * @param path path to service account key file
     *
     * @return side effect that evaluates to [[ServiceAccountKey]]
     */
    def readKey(path: String): ZIO[R, ServiceAccountKeyError, ServiceAccountKey]
  }
}
