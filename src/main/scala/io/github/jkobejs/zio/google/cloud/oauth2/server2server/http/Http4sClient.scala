package io.github.jkobejs.zio.google.cloud.oauth2.server2server.http

import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s._
import zio.Task
import zio.interop.catz._

trait Http4sClient extends HttpClient {
  implicit private val authResponseEntityDecoder = jsonOf[Task, HttpAuthResponse]
  val client: Client[Task]

  override val httpClient: HttpClient.Service[Any] =
    (request: HttpAuthRequest) =>
      (for {
        uri4s <- Task
                  .fromTry(Uri.fromString(request.uri).toTry)
                  .refineOrDie {
                    case error: ParseFailure =>
                      HttpError.UriParseError(error.sanitized)
                  }
        request4s = Request[Task](method = Method.POST, uri = uri4s).withEntity(
          UrlForm(
            "grant_type" -> request.grantType,
            "assertion"  -> request.jwtToken
          )
        )(UrlForm.entityEncoder[Task])
        response <- client.fetch[HttpAuthResponse](request4s)(
                     response =>
                       if (response.status.isSuccess)
                         response
                           .as[HttpAuthResponse]
                           .refineOrDie {
                             case failure: DecodeFailure => HttpError.ResponseParseError(failure.getMessage)
                           } else
                         for {
                           responseBody <- response.as[String](monadErrorInstance, EntityDecoder.text[Task])
                           result <- Task.fail(
                                      HttpError.HttpRequestError(
                                        status = response.status.toString(),
                                        body = responseBody
                                      )
                                    )
                         } yield result
                   )
      } yield response).refineOrDie { case e: HttpError => e }
}
