---
layout: home
---

# Google Oauth 2.0 for Scala

Effectful API for [Google OAuth 2.0][google-oauth] flows for Scala.

[![Build Status](https://travis-ci.com/jkobejs/zio-google-cloud-oauth2.svg?branch=master)](https://travis-ci.com/jkobejs/zio-google-cloud-oauth2)
[![Latest Version](https://maven-badges.herokuapp.com/maven-central/io.github.jkobejs/zio-google-cloud-oauth2_2.12/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A"io.github.jkobejs"%20zio-google-cloud-oauth2)
[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)


Google APIs use the OAuth 2.0 protocol for authentication and authorization. Google supports common OAuth 2.0 scenarios
such as those for web server, installed, and client-side applications.

Quick start
------------
The current version is **{{site.zioGoogleCloudOauth2Version}}** for **Scala 2.12/13** with
- [tsec][tsec] {{site.tsecVersion}}
- [http4s][http4s] {{site.http4sVersion}}
- [circe][circe] {{site.circeVersion}}
- [zio][zio] {{site.zioVersion}}
- [zio-macros][zio-macros] {{site.zioMacrosVersion}}
- [better-monadic-for][better-monadic-for] {{site.betterMonadicForVersion}}

To use library add this to `build.sbt` file:
```scala
libraryDependencies += "com.jkobejs" %% "zio-google-cloud-oauth2" % "{{site.zioGoogleCloudOauth2Version}}"
```

### Note on milestones:
Our Notation for versions is:
```
X.X.X
^ ^ ^____Minor
| |______Major
|________Complete redesign (i.e scalaz 7 vs 8)  
```

All `x.x.x-Mx` releases are milestone releases. Thus, we do not guarantee binary compatibility or no api-breakage until
a concrete version(i.e `0.0.1`). We aim to keep userland-apis relatively stable, but 
internals shift as we find better/more performant abstractions.

We will guarantee compatibility between minor versions (i.e 0.0.1 => 0.0.2) but not major versions (0.0.1 => 0.1.0)

Server to server
----------------
[Documentation][server-2-server] of server to server auth.

Web server applications
-----------------------
Not Implemented!


[google-oauth]: https://developers.google.com/identity/protocols/OAuth2
[tsec]: https://jmcardon.github.io/tsec/
[http4s]: https://http4s.org/
[server-2-server]: server-2-server
[circe]: https://circe.github.io/circe/
[zio]: https://zio.dev
[zio-macros]: https://github.com/zio/zio-macros
[better-monadic-for]: https://github.com/oleg-py/better-monadic-for
