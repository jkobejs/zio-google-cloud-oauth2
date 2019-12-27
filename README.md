# Google Oauth 2.0 for Scala

**zio-google-cloud-ouath2** is effectful API for [Google OAuth 2.0][google-oauth] flows for Scala.

Please proceed to the [microsite][microsite] for more information.

[google-oauth]: https://developers.google.com/identity/protocols/OAuth2
[microsite]: https://jkobejs.github.io/zio-google-cloud-oauth2/

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