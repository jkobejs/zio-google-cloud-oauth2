package io.github.jkobejs.zio.google.cloud.oauth2.http4s.server2server.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.Authenticator.Default
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.sign.TSecJwtSign
import io.github.jkobejs.zio.google.cloud.oauth2.http4s.server2server.http.Http4sClient
import zio.clock.Clock
import org.http4s.client.Client
import zio.Task
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator.Authenticator

trait Live extends Default with TSecJwtSign with Http4sClient with Clock.Live

object Live {
  def apply(clientT: Client[Task]): Authenticator = new Live {
    override val client: Client[Task] = clientT
  }
}
