package io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.authenticator

import io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator.Authenticator.Default
import io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.http.Http4sClient
import zio.clock.Clock
import org.http4s.client.Client
import zio.Task
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.authenticator.Authenticator

trait Live extends Default with Http4sClient with Clock.Live

object Live {
  def apply(clientT: Client[Task]): Authenticator = new Live {
    override val client: Client[Task] = clientT
  }
}
