package io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.common.UrlEncodedWriter

final case class AccessRequest(
  code: String,
  redirect_uri: String,
  client_id: String,
  client_secret: String,
  grant_type: String = "authorization_code"
)

object AccessRequest {
  implicit val urlEncodedWriterAccessRequest: UrlEncodedWriter[AccessRequest] = UrlEncodedWriter.gen
}
