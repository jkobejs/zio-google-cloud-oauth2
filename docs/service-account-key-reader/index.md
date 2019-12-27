---
layout: docs
title: Service account key reader
---

Service account key reader
----------------
`ServiceAccountKeyReader` provides api for reading service account key file from file system.


### Default
Service account key reader provides default implementation which uses FS2 streams in trait `FS2ServiceAccountKeyReader`.
Since it reads file from filesystem it requires blocking execution context which needs to be injected.

### Example

```scala mdoc:invisible
import io.github.jkobejs.zio.google.cloud.oauth2.serviceaccountkey._
import zio.blocking.Blocking
import zio._
```

```scala mdoc:silent
val service: ZIO[ServiceAccountKeyReader, ServiceAccountKeyError, ServiceAccountKey] = ServiceAccountKeyReader.>.readKey("src/test/resources/service-account.json")
val serviceAccountKey: IO[ServiceAccountKeyError, ServiceAccountKey] = service.provide(new FS2ServiceAccountKeyReader with Blocking.Live {})
```
