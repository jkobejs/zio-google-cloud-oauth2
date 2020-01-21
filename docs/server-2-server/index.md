---
layout: docs
title: Server to server
---

Server to server
----------------

![server-to-server][server-to-server-image]

The Google OAuth 2.0 system supports server-to-server interactions such as those between a web application and 
a Google service. For this scenario you need a service account, which is an account that belongs to your application
instead of to an individual end user. Your application calls Google APIs on behalf of the service account,
so users aren't directly involved.

### Usage
- [Create service account](#create-service-account)
- [Read service account key (optional)](#read-service-account-key-optional)
- [Authenticate](#authenticate)
- [Integration tests](#integration-tests)

#### Create service account
To support server-to-server interactions, first create a [service account][service-account] for your project in the Google API Console.
A service account's credentials include a generated email address that is unique and at least 
one public/private key pair.
If domain-wide delegation is enabled, then a client ID is also part of the service account's credentials.


#### Read service account key (optional)
This step is optional. Service account private key, token uri and client email can be provided in multiple ways 
(env vars, config, etc..).
This lib offers api to read service account key Json data from file system.

Service account key reader provides default implementation which uses FS2 streams in trait FS2ServiceAccountKeyReader. 
Since it reads file from filesystem it requires blocking execution context which is provided by extending
`zio.blocking.Blocking.Live` module.

```scala mdoc:invisible
import java.util.concurrent.TimeUnit
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.authenticator._
import zio._
import zio.blocking._
import zio.interop.catz._
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey._
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
val serviceAccountKeyReader: ZIO[ServiceAccountKeyReader, ServiceAccountKeyError, ServiceAccountKey] = ServiceAccountKeyReader.>.readKey("path-to-service-account-key")
val serviceAccountKey: IO[ServiceAccountKeyError, ServiceAccountKey] = serviceAccountKeyReader.provide(new FS2ServiceAccountKeyReader with Blocking.Live {})
```

#### Authenticate
Server to server authentication is exposed in `Authenticator` module through service method
```scala
def auth(cloudApiConfig: CloudApiConfig, cloudApiClaims: CloudApiClaims): ZIO[R, AuthenticatorError, AuthResponse]
```

It receives two parameters, `CloudApiConfig` and `CloudApiClaims`:

```scala
/**
 * Represents config used to connect to Google OAuth 2.0 server.
 *
 * @param uri url used for creating auth requests
 * @param privateKey private key used to sign JWT token
 * @param grantType given grant
 */
final case class CloudApiConfig(
  uri: String,
  privateKey: String,
  grantType: String
)
```

```scala
/**
 * Represents the JWT Claims used in Google server-to-server oauth
 *
 *
 * @param issuer Issuer claim, Case insensitive
 * @param scope A space-delimited list of the permissions that the application requests
 * @param audience The audience Case-sensitive. Can be either a list or a single string
 * @param subject Subject, Case-sensitive string when defined
 * @param expiresIn Controls when auth token will expire (Google API default is 1 hour)
 */
final case class CloudApiClaims(
  issuer: String,
  scope: String,
  audience: String,
  subject: Option[String] = None,
  expiresIn: Duration = Duration(1, TimeUnit.HOURS)
)
```

On success auth method returns:
```scala
/**
 * Represents Authorization Server access token response.
 *
 * Access token expires in one hour and can be reused until they expire.
 *
 * @param accessToken google access token
 * @param tokenType token type
 * @param expiresAt when will token expire
 */
final case class AuthResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant
)
```

Module contains live implementation in `Authenticator.Live` that depends only on `org.http4s.client.Client` which is 
needed to make http requests.

```scala mdoc:silent
val authenticatorLiveManaged: ZManaged[Any, Throwable, Authenticator.Live] = ZIO
  .runtime[Any]
  .toManaged_
  .flatMap { implicit rts =>
    BlazeClientBuilder[Task](rts.platform.executor.asEC)
      .resource
      .toManaged
      .map(
        client4s =>
          new Authenticator.Live {
            val client: Client[Task] = client4s
          }
      )
  } 

val authResponse: ZIO[Any, Throwable, AuthResponse] = Authenticator.>.auth(apiConfig, apiClaims).provideManaged(authenticatorLiveManaged)
```

###### Caching
Even though [Google Cloud server to server][google-server-to-server] says that access tokens expire after one hour
authenticator doesn't cache auth responses by itself, it is left for user to decide how to keep tokens.
Since authenticator returns auth responses wrapped in zio effect it is easy to cache them by using builtin API:
```scala mdoc:silent
val cached: ZIO[Authenticator with clock.Clock, Nothing, IO[AuthenticatorError, AuthResponse]] = Authenticator.>.auth(apiConfig, apiClaims).cached(duration.Duration(1, TimeUnit.HOURS))
```

###### Modularity
Server to server api is modular and exposes these zio modules, [Authenticator][authenticator], 
[JwtSign][jwt-sign] and [HttpClient][http-client]. With this approach users can easily switch between their own 
implementations or the ones that library offers. 

If for example user sees value in using different http client 
all that is necessary is to implement 
```scala
def auth(request: HttpAuthRequest): ZIO[R, HttpError, HttpAuthResponse]
```
method in `HttpClient.Service[R]` service. Method receives parameter of type `HttpAuthRequest`:
```scala
case class HttpAuthRequest(
  jwtToken: String,
  uri: String,
  grantType: String
)
```

On success it returns `HttpAuthResponse`:
```scala
final case class HttpAuthResponse(
  access_token: String,
  token_type: String,
  expires_in: Long
)
```

If for example user sees value in using different jwt signer all that is necessary is to implement:
```scala
def sign(privateKey: String, claims: Claims): ZIO[R, JwtSignError, JwtToken]
```
method in `JwtSign.Service[R]` service. Method receives service account private key and claims:
```scala
final case class Claims(
  issuer: String,
  scope: String,
  audience: String,
  expiration: Instant,
  issuedAt: Instant,
  subject: Option[String] = None
)
```

On success it returns `JwtToken`:
```scala
final case class JwtToken(token: String)
```
 
 
###### Default
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

Authenticator live implementation `Authenticator.Live` extends `Default` implementation and uses these modules: 
 - `TsetJwtSign` implementation of `JwtSign` module
 - `Http4sClient` implementation of `HttpClient` module
 - `Clock.Live` implementation of `Clock` module

To use `Authenticator.Live` implementation user needs to provide instance of `org.http4s.client.Client`.

#### Integration tests
To run integration tests together with unit tests you need to export path to service account key Json file in 
`SERVICE_ACCOUNT_KEY_PATH` env variable.
```bash
export SERVICE_ACCOUNT_KEY_PATH=/.../service-account-key.json

sbt test
```


[server-to-server-image]: serviceaccount.png
[authenticator]: https://github.com/jkobejs/zio-google-cloud-oauth2/blob/master/src/main/scala/io/github/jkobejs/zio/google/cloud/oauth2/server2server/authenticator/Authenticator.scala
[jwt-sign]: https://github.com/jkobejs/zio-google-cloud-oauth2/blob/master/src/main/scala/io/github/jkobejs/zio/google/cloud/oauth2/server2server/sign/JwtSign.scala
[http-client]: https://github.com/jkobejs/zio-google-cloud-oauth2/blob/master/src/main/scala/io/github/jkobejs/zio/google/cloud/oauth2/server2server/http/HttpClient.scala
[authenticator-error]: https://github.com/jkobejs/zio-google-cloud-oauth2/blob/master/src/main/scala/io/github/jkobejs/zio/google/cloud/oauth2/server2server/authenticator/AuthenticatorError.scala
[google-server-to-server]: https://developers.google.com/identity/protocols/OAuth2ServiceAccount
[service-account]: https://cloud.google.com/iam/docs/understanding-service-accounts