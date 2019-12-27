---
layout: home
title: "Home"
section: "home"
---

# Google Oauth 2.0 for Scala

Effectfull API for [Google OAuth 2.0][google-oauth] flows for Scala. Currently it supports only server to server interaction.

Quick start
------------
The current version is **{{site.googleOauth4sVersion}}** for **Scala 2.12/13** with
- [tsec][tsec] {{site.tsecVersion}}
- [http4s][http4s] {{site.http4sVersion}}
- [circe][circe] {{site.circeVersion}}
- [zio][zio] {{site.zioVersion}}
- [zio-macros][zio-macros] {{site.zioMacrosVersion}}
- [better-monadic-for][better-monadic-for] {{site.betterMonadicForVersion}}

To use library publish it locally.
```scala
libraryDependencies += "com.jkobejs" %% "zio-google-cloud-oauth2" % "{{site.zioGoogleCloudOauth2Version}}"
```

Server to server
----------------
[Documentation][server-2-server] of server to server auth.


[google-oauth]: https://developers.google.com/identity/protocols/OAuth2
[tsec]: https://jmcardon.github.io/tsec/
[http4s]: https://http4s.org/
[server-2-server]: server-2-server
[circe]: https://circe.github.io/circe/
[zio]: https://zio.dev
[zio-macros]: https://github.com/zio/zio-macros
[better-monadic-for]: https://github.com/oleg-py/better-monadic-for
