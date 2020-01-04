package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.common.UrlEncodedWriter

final case class RefreshRequest(
  client_secret: String,
  refresh_token: String,
  client_id: String,
  grant_type: String = "refresh_token"
)

object RefreshRequest {
  implicit val urlEncodedWriterRefreshRequest: UrlEncodedWriter[RefreshRequest] = UrlEncodedWriter.gen
}
