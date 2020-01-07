package io.github.jkobejs.zio.google.cloud.oauth2.utils

import cats.data.Kleisli
import io.circe.Encoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{EntityEncoder, HttpRoutes, Request, Response}
import zio._
import zio.interop.catz.taskConcurrentInstance

class RedirectionRouter(queue: Queue[String]) {
  val zioHttp4sDsl: Http4sDsl[Task] = new Http4sDsl[Task] {}
  import zioHttp4sDsl._
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] =
    jsonEncoderOf[Task, A]

  val redirectionRoute: HttpRoutes[Task] = HttpRoutes.of[Task] {
    case req @ GET -> Root =>
      val code             = req.uri.query.params.getOrElse("code", req.uri.query.params("error"))
      val q: Task[Boolean] = queue.offer(code)
      Ok(q)
  }

  val redirectionApp: Kleisli[Task, Request[Task], Response[Task]] = Router("/" -> redirectionRoute).orNotFound
}

object RedirectionRouter {
  def apply(queue: Queue[String]): RedirectionRouter = new RedirectionRouter(queue)
}
