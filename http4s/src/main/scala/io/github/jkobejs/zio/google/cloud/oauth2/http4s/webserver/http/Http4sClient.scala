/*
 * Copyright 2019 Josip Grgurica and Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jkobejs.zio.google.cloud.oauth2.http4s.webserver.http

import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import zio.interop.catz._
import zio.{Task, ZIO}
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http._

trait Http4sClient extends HttpClient {
  implicit private val httpAccessRequestEncoder   = jsonEncoderOf[Task, HttpAccessRequestBody]
  implicit private val httpAccessResponseDecoder  = jsonOf[Task, HttpAccessResponse]
  implicit private val httpRefreshRequestEncoder  = jsonEncoderOf[Task, HttpRefreshRequestBody]
  implicit private val httpRefreshResponseDecoder = jsonOf[Task, HttpRefreshResponse]

  val client: Client[Task]

  override val httpClient: HttpClient.Service[Any] = new HttpClient.Service[Any] {
    override def authenticate(request: HttpAccessRequest): ZIO[Any, HttpError, HttpAccessResponse] =
      (for {
        uri4s <- Task
                  .fromTry(Uri.fromString(request.uri).toTry)
                  .refineOrDie {
                    case error: ParseFailure =>
                      HttpError.UriParseError(error.sanitized)
                  }
        request4s = Request[Task](method = Method.POST, uri = uri4s).withEntity(
          request.httpAccessRequestBody
        )
        response <- client.fetch[HttpAccessResponse](request4s)(
                     response =>
                       if (response.status.isSuccess)
                         response
                           .as[HttpAccessResponse]
                           .refineOrDie {
                             case failure: DecodeFailure => HttpError.ResponseParseError(failure.getMessage)
                           }
                       else
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

    override def refreshToken(request: HttpRefreshRequest): ZIO[Any, HttpError, HttpRefreshResponse] =
      (for {
        uri4s <- Task
                  .fromTry(Uri.fromString(request.uri).toTry)
                  .refineOrDie {
                    case error: ParseFailure =>
                      HttpError.UriParseError(error.sanitized)
                  }
        request4s = Request[Task](method = Method.POST, uri = uri4s).withEntity(
          request.httpRefreshRequestBody
        )
        response <- client.fetch[HttpRefreshResponse](request4s)(
                     response =>
                       if (response.status.isSuccess)
                         response
                           .as[HttpRefreshResponse]
                           .refineOrDie {
                             case failure: DecodeFailure => HttpError.ResponseParseError(failure.getMessage)
                           }
                       else
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
}

object Http4sClient {
  def apply(clientT: Client[Task]): HttpClient = new Http4sClient {
    override val client: Client[Task] = clientT
  }
}
