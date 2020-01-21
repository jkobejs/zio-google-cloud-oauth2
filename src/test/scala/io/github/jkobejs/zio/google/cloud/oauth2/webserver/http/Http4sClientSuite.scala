package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http

import cats.effect.Resource
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.Fixtures
import org.http4s.Response
import org.http4s.client.Client
import zio.Task
import zio.interop.catz._
import zio.test.Assertion.equalTo
import zio.test.{assertM, suite, testM}

object Http4sClientSuite {
  val http4sClientSuite = suite("Http4s Client tests")(
    testM("http4s client fails for invalid uri") {
      val client4s = Client[Task](_ => null)

      val http4sClient = new Http4sClient {
        override val client = client4s
      }

      val req = Fixtures.httpAccessRequest.copy(uri = "https:/ /uri")

      assertM(http4sClient.httpClient.authenticate(req).either, equalTo(Left(HttpError.UriParseError("Invalid URI"))))
    },
    testM("http4 client fails when response is not success") {
      val client4s = Client[Task](_ => Resource.liftF[Task, Response[Task]](Task.succeed(Response.notFound[Task])))

      val http4sClient = new Http4sClient {
        override val client = client4s
      }

      assertM(
        http4sClient.httpClient.authenticate(Fixtures.httpAccessRequest).either,
        equalTo(Left(HttpError.HttpRequestError(status = "404 Not Found", body = "Not found")))
      )
    },
    testM("http4 client fails when response body is invalid") {
      val client4s = Client[Task](
        _ =>
          Resource.liftF[Task, Response[Task]](
            Task.succeed(Response[Task](body = fs2.Stream.fromIterator[Task]("test".getBytes().iterator)))
          )
      )

      val http4sClient = new Http4sClient {
        override val client = client4s
      }

      assertM(
        http4sClient.httpClient.authenticate(Fixtures.httpAccessRequest).either,
        equalTo(Left(HttpError.ResponseParseError("Malformed message body: Invalid JSON")))
      )
    },
    testM("http4 client authentication succeeds for valid data") {

      val client4s = Client[Task](
        _ =>
          Resource.liftF[Task, Response[Task]](
            Task.succeed(
              Response[Task](
                body = fs2.Stream.fromIterator[Task](
                  s"""
                     |{
                     |  "access_token": "${Fixtures.httpAccessResponse.access_token}",
                     |  "token_type": "${Fixtures.httpAccessResponse.token_type}",
                     |  "expires_in": ${Fixtures.httpAccessResponse.expires_in},
                     |  "refresh_token" : "${Fixtures.httpAccessResponse.refresh_token}"
                     |}
                     |""".stripMargin
                    .getBytes()
                    .iterator
                )
              )
            )
          )
      )

      val http4sClient = new Http4sClient {
        override val client = client4s
      }

      assertM(http4sClient.httpClient.authenticate(Fixtures.httpAccessRequest), equalTo(Fixtures.httpAccessResponse))
    },
    testM("http4 client refresh token succeeds for valid data") {

      val client4s = Client[Task](
        _ =>
          Resource.liftF[Task, Response[Task]](
            Task.succeed(
              Response[Task](
                body = fs2.Stream.fromIterator[Task](
                  s"""
                     |{
                     |  "access_token": "${Fixtures.httpRefreshResponse.access_token}",
                     |  "token_type": "${Fixtures.httpRefreshResponse.token_type}",
                     |  "expires_in": ${Fixtures.httpRefreshResponse.expires_in}
                     |}
                     |""".stripMargin
                    .getBytes()
                    .iterator
                )
              )
            )
          )
      )

      val http4sClient = new Http4sClient {
        override val client = client4s
      }

      assertM(http4sClient.httpClient.refreshToken(Fixtures.httpRefreshRequest), equalTo(Fixtures.httpRefreshResponse))
    }
  )
}
