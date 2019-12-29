package io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey

import java.nio.file.{InvalidPathException, NoSuchFileException, Paths}

import cats.effect.Blocker
import io.circe.generic.auto._
import io.circe.parser.decode
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey.ServiceAccountKeyError.{
  FileDoesNotExist,
  InvalidJsonFormat,
  InvalidPathError
}
import io.github.jkobejs.zio.google.cloud.oauth2.server2server.serviceaccountkey.ServiceAccountKeyReader.Service
import zio.blocking.Blocking
import zio.interop.catz._
import zio.{RIO, Task, ZIO}

trait FS2ServiceAccountKeyReader extends ServiceAccountKeyReader with Blocking {
  override val serviceAccountKeyReader: Service[Any] = new Service[Any] {
    override def readKey(path: String): ZIO[Any, ServiceAccountKeyError, ServiceAccountKey] =
      blocking.blockingExecutor.flatMap(
        executor =>
          RIO
            .effect(Paths.get(path))
            .refineOrDie {
              case _: InvalidPathException => InvalidPathError(path)
            }
            .flatMap(
              jPath =>
                fs2.io.file
                  .readAll[Task](jPath, Blocker.liftExecutionContext(executor.asEC), 4096)
                  .through(fs2.text.utf8Decode)
                  .compile
                  .toList
                  .map(_.mkString)
                  .refineOrDie {
                    case _: NoSuchFileException => FileDoesNotExist(path)
                  }
                  .flatMap(
                    string =>
                      RIO
                        .fromEither(decode[ServiceAccountKey](string))
                        .mapError(_ => InvalidJsonFormat(path))
                  )
            )
            .refineOrDie {
              case error: ServiceAccountKeyError => error
            }
      )
  }
}
