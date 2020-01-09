---
layout: docs
title: Web server
---

OAuth 2.0 Server-side Web Apps
----------------

![web-server][webflow-image]

OAuth 2.0 allows users to share specific data with an application while keeping their usernames, passwords, and other information private. For example, an application can use OAuth 2.0 to obtain permission from users to store files in their Google Drives

This OAuth 2.0 flow is specifically for user authorization. It is designed for applications that can store confidential information and maintain state. A properly authorized web server application can access an API while the user interacts with the application or after the user has left the application.


### Usage
- [Enable APIs for your project](#enable-apis-for-your-project)
- [Create authorization credentials](#create-authorization-credentials)
- [Read OAuth 2.0 Client key (optional)](#read-oauth-20-client-key-_optional_)
- [Authorize](#authorize)
- [Authenticate](#authenticate)
- [Refresh an access token (offline access)](#refresh-an-access-token-offline-access)
- [Integration tests](#integration-tests)

#### Create service account
To support server-to-server interactions, first create a [service account][service-account] for your project in the Google API Console.
A service account's credentials include a generated email address that is unique and at least 
one public/private key pair.
If domain-wide delegation is enabled, then a client ID is also part of the service account's credentials.

### Prerequisites

#### Enable APIs for your project

Any application that calls Google APIs needs to enable those APIs in the API Console. To enable the appropriate APIs for your project:

1. Open the [Library][google-library] page in the API Console.
2. Select the project associated with your application. Create a project if you do not have one already.
3. Use the Library page to find each API that your application will use. Click on each API and enable it for your project.

#### Create authorization credentials

Any application that uses OAuth 2.0 to access Google APIs must have authorization credentials that identify the application to Google's OAuth 2.0 server. The following steps explain how to create credentials for your project. Your applications can then use the credentials to access APIs that you have enabled for that project.

1. Open the [Credentials page][credentials-page] in the API Console.
2. Click **Create credentials > OAuth client ID**.
3. Complete the form. Set the application type to Web application. You must specify authorized redirect URIs. The redirect URIs are the endpoints to which the OAuth 2.0 server can send responses.

It is best that you [design your app's auth endpoints][protect-authcode] so that your application does not expose authorization codes to other resources on the page.

After creating your credentials, download the `client_secret.json` file from the API Console. Securely store the file in a location that only your application can access.

> **Important:** Do not store the `client_secret.json` file in a publicly-accessible location. In addition, if you share the source code to your application — for example, on GitHub — store the `client_secret.json` file outside of your source tree to avoid inadvertently sharing your client credentials.

### Using the library

#### Read OAuth 2.0 Client key _(optional)_
This step is optional. OAuth Client key, token URI, etc. can be provided in multiple ways (env vars, config, etc..).
This lib offers API to read client key (`client_secret.json`) JSON data from file system.

Client key reader provides default implementation which uses FS2 streams in trait `FS2OAuthClientKeyReader`. 
Since it reads file from filesystem, it requires blocking execution context which is provided by extending `zio.blocking.Blocking.Live` module.

```scala mdoc:invisible
import java.util.concurrent.TimeUnit
import io.github.jkobejs.zio.google.cloud.oauth2.webserver.oauthclientkey.{FS2OAuthClientKeyReader, OAuthClientKey, OAuthClientKeyError, OAuthClientKeyReader}
import zio._
import zio.blocking.Blocking

val oAuthClientKeyReader: ZIO[OAuthClientKeyReader, OAuthClientKeyError, OAuthClientKey] = OAuthClientKeyReader.>.readKey("/path/to/client_secret.json")
val oAuthClientKey: IO[OAuthClientKeyError, OAuthClientKey] = oAuthClientKeyReader.provide(new FS2OAuthClientKeyReader with Blocking.Live {})
```

#### Authorize

To request for an access token, you must have authorization code which is obtained by providing to user an authorization URI which will ask for his consent.
The OAuth 2.0 server responds to your application's access request by using the URL specified in the request (`redirect_uri`).

If the user approves the access request, then the response contains an authorization code. If the user does not approve the request, the response contains an error message. The authorization code or error message that is returned to the web server appears on the query string, as shown below:

An error response:
```
https://oauth2.example.com/auth?error=access_denied
```

An authorization code response:
```
https://oauth2.example.com/auth?code=4/P7q7W91a-oMsCeLvIaQm6bTrgtp7
```

The lib user is responsible for handling this URL callback and parsing the code/error from the request's query param. 

You generate this authorization URL with the following method:
```scala
def createAuthUrl(cloudApiConfig: CloudApiConfig, authRequestParams: AuthRequestParams): String
```

It receives two parameters, `CloudApiConfig` and `AuthRequestParams`:

```scala
/**
 * Represents config used to connect to Google OAuth 2.0 server.
 *
 * @param client_id Google OAuth 2.0 Client ID
 * @param project_id Google project ID
 * @param auth_uri url used for creating authorization requests (obtaining authorization code)
 * @param token_uri url used for creating authentication requests (obtaining access and refresh tokens)
 * @param client_secret Google client password
 * @param redirect_uris List of redirect URIs, must be verified in Google Console
 */
final case class CloudApiConfig(
  client_id: String,
  project_id: String,
  auth_uri: String,
  token_uri: String,
  client_secret: String,
  redirect_uris: NonEmptyList[String]
)

/**
 * Authentication request parameters. Used to construct [[AuthRequest]]
 *
 * @param scope A space-delimited list of scopes that identify the resources that your application could access on the user's behalf
 * @param redirect_uri  Determines where the API server redirects the user after the user completes the authorization flow
 * @param access_type Indicates whether your application can refresh access tokens when the user is not present at the browser (online or offline)
 * @param state Specifies any string value that your application uses to maintain state between your authorization request and the authorization server's response. The server returns the exact value that you send as a name=value pair in the hash (#) fragment of the redirect_uri after the user consents to or denies your application's access request
 * @param include_granted_scopes Enables applications to use incremental authorization to request access to additional scopes in context
 * @param login_hint The server uses the hint to simplify the login flow either by prefilling the email field in the sign-in form or by selecting the appropriate multi-login session
 * @param prompt A space-delimited, case-sensitive list of prompts to present the user. If you don't specify this parameter, the user will be prompted only the first time your app requests access. Possible values are: none, consent, select_account
 * @param response_type Response type - should be set to "code" for this type of requests
 */
final case class AuthRequestParams(
  scope: String,
  redirect_uri: Option[String] = None,
  access_type: String = "offline",
  state: Option[String] = None,
  include_granted_scopes: Option[Boolean] = None,
  login_hint: Option[String] = None,
  prompt: Option[String] = Some("consent"),
  response_type: String = "code"
)
```

#### Authenticate
Web server authentication is exposed in `Authenticator` module through service method
```scala
def authenticate(cloudApiConfig: CloudApiConfig, authorizationCode: String): ZIO[R, AuthenticationError, AccessResponse]
```

It receives two parameters, `CloudApiConfig` and `authorizationCode` (obtained from the [previous](#authorize) step).

On success, `authentication` method returns:
```scala
/**
 * Represents Authorization Server authentication response, having both access and refresh token.
 *
 * Access token expires in one hour and can be reused until it expires.
 * @param accessToken google access token
 * @param tokenType token type
 * @param expiresAt when will token expire
 * @param refreshToken google refresh token
 */
final case class AccessResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant,
  refreshToken: String
)
```

Module contains live implementation in `Authenticator.Live` that depends only on `org.http4s.client.Client` which is needed to make http requests.

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

val accessResponse: ZIO[Any, AuthenticationError, AccessResponse] = Authenticator.>.authenticate(cloudApiConfig, authorizationCode).provideManaged(authenticatorLiveManaged)
```

#### Refresh an access token (offline access)

Web server access token refresh is exposed in `Authenticator` module through service method:

```scala
def refreshToken(cloudApiConfig: CloudApiConfig, refreshToken: String): ZIO[R, AuthenticationError, RefreshResponse]
```

It receives two parameters, `CloudApiConfig` and `refreshToken` obtained from the `AccessResponse`.

On success, `refreshToken` method returns `RefreshResponse`:
```scala
/**
 * Response from refresh token request.
 *
 * @param accessToken google access token
 * @param tokenType token type
 * @param expiresAt when will token expire
 */
final case class RefreshResponse(
  accessToken: String,
  tokenType: String,
  expiresAt: Instant
)
```

Access tokens periodically expire. You can refresh an access token without prompting the user for permission (including when the user is not present) if you requested offline access to the scopes associated with the token (default with this library).
Refresh token does not expire so it is advisable to persist it to database/file system so you can use it to access a Google API when the user is not present.

To refresh the access token, you can use the same `Authenticator.Live` managed resource as above:
```scala mdoc:silent
val refreshResponse: ZIO[Any, AuthenticationError, RefreshResponse] = Authenticator.>.refreshToken(cloudApiConfig, accessResponse.refreshToken).provideManaged(authenticatorLiveManaged)
```

###### Caching
Even though [OAuth 2.0 for Web Server Applications][google-web-server] says that access tokens expire after one hour, authenticator doesn't cache auth responses by itself.
It is left for user to decide how to keep tokens and when to [refresh](#refresh-an-access-token-offline-access) them.
Since authenticator returns authentication responses wrapped in ZIO effect, it is easy to cache them by using builtin API:
```scala mdoc:silent
val cached: ZIO[Authenticator with clock.Clock, Nothing, IO[AuthenticationError, AccessResponse]] = Authenticator.>.authenticate(cloudApiConfig, authorizationCode).cached(duration.Duration(1, TimeUnit.HOURS))
```

###### Modularity
Web server API is modular and exposes these ZIO modules, [Authenticator][authenticator] and [HttpClient][http-client].
With this approach users can easily switch between their own implementations or the ones that library offers. 

If for example user sees value in using different http client, all that is necessary is to implement 
```scala
def authenticate(request: HttpAccessRequest): ZIO[R, HttpError, HttpAccessResponse]

def refreshToken(request: HttpRefreshRequest): ZIO[R, HttpError, HttpRefreshResponse]
```
methods in `HttpClient.Service[R]` service.

Method `authenticate` receives parameter of type `HttpAuthRequest`:
```scala
final case class HttpAccessRequest(uri: String, httpAccessRequestBody: HttpAccessRequestBody)

final case class HttpAccessRequestBody(
  code: String,
  redirect_uri: String,
  client_id: String,
  client_secret: String,
  grant_type: String = "authorization_code"
)
```

On success it returns `HttpAccessResponse`:
```scala
final case class HttpAccessResponse(
  access_token: String,
  token_type: String,
  expires_in: Long,
  refresh_token: String
)
```
Method `refreshToken` receives parameter of type `HttpRefreshRequest`:
```scala
case class HttpRefreshRequest(uri: String, httpRefreshRequestBody: HttpRefreshRequestBody)

final case class HttpRefreshRequestBody(
  refresh_token: String,
  client_id: String,
  client_secret: String,
  grant_type: String = "refresh_token"
)
```

On success it returns `HttpRefreshResponse`:
```scala
final case class HttpRefreshResponse(
  access_token: String,
  token_type: String,
  expires_in: Long
)
```
 
###### Default
Authenticator module offers default implementation in trait `Authenticator.Default` which depends on
 - HttpClient
 - zio.clock.Clock
 
services. 

```scala
trait Default extends Authenticator {
  self =>
  val httpClient: HttpClient.Service[Any]
  val clock: Clock.Service[Any]
  ...
}
```

Authenticator live implementation `Authenticator.Live` extends `Default` implementation and uses these modules: 
 - `Http4sClient` implementation of `HttpClient` module
 - `Clock.Live` implementation of `Clock` module

To use `Authenticator.Live` implementation user needs to provide instance of `org.http4s.client.Client`.

#### Integration tests
To run integration tests together with unit tests you need to export path to client key JSON file in 
`OAUTH_CLIENT_KEY_PATH` env variable.
```bash
export OAUTH_CLIENT_KEY_PATH=/.../client_secret.json

sbt test
```
If `OAUTH_CLIENT_KEY_PATH` env variable is provided with valid client key file, the integration test will open the browser where you'll need to consent to using the app.
After consent, Google auth server will send authorization code to redirect URI that you provided in [prerequisites](#prerequisites) step.
When testing, integration test expects that URI to be `http://localhost:8080` so it can continue with authentication and refresh token request tests.

[webflow-image]: webflow.png
[google-library]: https://console.developers.google.com/apis/library
[credentials-page]: https://console.developers.google.com/apis/credentials
[protect-authcode]: https://developers.google.com/identity/protocols/OAuth2WebServer#protectauthcode
[google-web-server]: https://developers.google.com/identity/protocols/OAuth2WebServer
[authenticator]: https://github.com/jkobejs/zio-google-cloud-oauth2/blob/master/src/main/scala/io/github/jkobejs/zio/google/cloud/oauth2/webserver/authenticator/Authenticator.scala
[http-client]: https://github.com/jkobejs/zio-google-cloud-oauth2/blob/master/src/main/scala/io/github/jkobejs/zio/google/cloud/oauth2/webserver/http/HttpClient.scala
