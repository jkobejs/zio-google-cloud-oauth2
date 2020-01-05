package io.github.jkobejs.zio.google.cloud.oauth2.webserver.http
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.http.HttpError.{HttpRequestError, UriParseError}
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.circe._
import sttp.model.Uri
import zio.ZIO

trait SttpClient extends HttpClient {

  private val backend = AsyncHttpClientZioBackend()

  override val httpClient: HttpClient.Service[Any] = new HttpClient.Service[Any] {

    def decodeAndRefineErrors[T](
      response: Either[Throwable, Response[Either[String, String]]]
    )(implicit decoder: Decoder[T]): Either[HttpError, T] =
      response.left
        .map(e => HttpRequestError("Failed effect", e.getMessage))
        .flatMap(
          response =>
            response.body match {
              case Left(error) =>
                Left(HttpRequestError(response.statusText, error))
              case Right(value) =>
                decode(value) match {
                  case Left(parseError) => Left(UriParseError(parseError.getMessage))
                  case Right(t)         => Right(t)
                }
            }
        )

    override def authenticate(request: HttpAccessRequest): ZIO[Any, HttpError, HttpAccessResponse] =
      backend
        .flatMap { implicit be =>
          ZIO
            .fromEither(Uri.parse(request.uri))
            .mapError(UriParseError)
            .flatMap(
              uri =>
                ZIO.absolve(
                  basicRequest
                    .post(uri)
                    .body(request.httpAccessRequestBody)
                    .send()
                    .either
                    .map(r => decodeAndRefineErrors[HttpAccessResponse](r))
                )
            )
        }
        .refineOrDie {
          case e: HttpError => e
        }

    override def refreshToken(request: HttpRefreshRequest): ZIO[Any, HttpError, HttpRefreshResponse] =
      backend
        .flatMap { implicit be =>
          ZIO
            .fromEither(Uri.parse(request.uri))
            .mapError(UriParseError)
            .flatMap(
              uri =>
                ZIO.absolve(
                  basicRequest
                    .post(uri)
                    .body(request.httpRefreshRequestBody)
                    .send()
                    .either
                    .map(r => decodeAndRefineErrors[HttpRefreshResponse](r))
                )
            )
        }
        .refineOrDie {
          case e: HttpError => e
        }
  }
}
