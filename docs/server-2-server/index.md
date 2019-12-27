---
layout: docs
title: Server to server
---

Server to server
----------------

To support server-to-server interactions, first create a service account for your project in the Google API Console.

Server to server interaction is modular and exposes these zio modules, [Authenticator][authenticator], 
[JwtSign][jwt-sign] and [HttpClient][http-client].

`Authenticator` is main module and it exposes service with auth method which receives cloud api config and cloud api 
claims and returns `AuthResponse` with access token inside zio effect which is parametrized with `AuthenticatorError`
error type.
```scala 
val authResponse: ZIO[R, AuthenticatorError, AuthResponse]
``` 

### Default
Authenticator module offers default implementation in trait `Authenticator.Default` which depends on
 - JwtSign
 - HttpClient
 - zio.clock.Clock
 
services. 

```scala
trait Default extends Authenticator {
  self =>
  val jwtSign: JwtSign.Service[Any]
  val httpClient: HttpClient.Service[Any]
  val clock: Clock.Service[Any]

  ...
}
```

### Live

Authenticator module also offers live implementation of `Default` trait in trait `Authenticator.Live` 
which injects:
 - `TsetJwtSign` implementation of `JwtSign` module
 - `Http4sClient` implementation of `HttpClient` module
 - `Clock.Live` implementation of `Clock` module

To use `Authenticator.Live` implementation user needs to provide instance of `org.http4s.client.Client`.

### Modularity
Server to server api is extremely modular and with this approach users can easily switch between their own 
implementations or the ones that library offers. If for example user sees value in using different http client 
all that is necessary is to implement 
```scala
def auth(request: HttpAuthRequest): ZIO[R, HttpError, HttpAuthResponse]
```
method in `HttpClient.Service[Any]` and inject it in `Authenticator.Default` implementation. The same is for `JtwSign` service.

### Caching
Even though [Google Cloud server to server][google-server-to-server] says that access tokens expire after one hour
authenticator doesn't cache auth responses by itself, it is left for user to decide. Since authenticator returns
auth responses wrapped in zio effect it is easy to cache them by using builtin API:

```scala mdoc:invisible
import java.util.concurrent.TimeUnit
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator._
import zio._
import zio.blocking._
import zio.interop.catz._
import io.github.jkobejs.zio.google.cloud.oauth2.serviceaccountkey._
import org.http4s.client._
import org.http4s.client.blaze._

val apiConfig: CloudApiConfig = CloudApiConfig(
  uri        = "serviceAccountKey.token_uri",
  privateKey = "serviceAccountKey.private_key",
  grantType  = "urn:ietf:params:oauth:grant-type:jwt-bearer"
)
val apiClaims: CloudApiClaims = CloudApiClaims(
  issuer   = "serviceAccountKey.client_email",
  scope    = "https://www.googleapis.com/auth/devstorage.read_write",
  audience = "serviceAccountKey.token_uri"
)
```

```scala mdoc:silent
val cached: ZIO[Authenticator with clock.Clock, Nothing, IO[AuthenticatorError, AuthResponse]] = Authenticator.>.auth(apiConfig, apiClaims).cached(duration.Duration(1, TimeUnit.HOURS))
```

### Live example
```scala mdoc:silent
val serviceAccountKeyReader: ZIO[ServiceAccountKeyReader, ServiceAccountKeyError, ServiceAccountKey] = ServiceAccountKeyReader.>.readKey("path-to-service-account-key")

val authenticatorLiveManaged: ZManaged[Any, Throwable, Authenticator.Live] = ZIO
  .runtime[Any]
  .toManaged_
  .flatMap { implicit rts =>
    val exec = rts.platform.executor.asEC
    BlazeClientBuilder[Task](exec)
      .resource
      .toManaged
      .map(
        client4s =>
          new Authenticator.Live {
            val client: Client[zio.Task] = client4s
          }
      )
  } 

val authResponse: ZIO[Any, Throwable, AuthResponse] = for {
  serviceAccountKey <- serviceAccountKeyReader.provide(new FS2ServiceAccountKeyReader with Blocking.Live {})
  cloudApiConfig    = CloudApiConfig(
    uri        = serviceAccountKey.token_uri,
    privateKey = serviceAccountKey.private_key,
    grantType  = "urn:ietf:params:oauth:grant-type:jwt-bearer"
  )
  cloudApiClaims    = CloudApiClaims(
    issuer   = serviceAccountKey.client_email,
    scope    = "https://www.googleapis.com/auth/devstorage.read_write",
    audience = serviceAccountKey.token_uri
  )
  authResponse     <- Authenticator.>.auth(cloudApiConfig, cloudApiClaims).provideManaged(authenticatorLiveManaged)
} yield authResponse
```


[authenticator]: 
[jwt-sign]: 
[http-client]: 
[authenticator-error]: error
[google-server-to-server]: https://developers.google.com/identity/protocols/OAuth2ServiceAccount
