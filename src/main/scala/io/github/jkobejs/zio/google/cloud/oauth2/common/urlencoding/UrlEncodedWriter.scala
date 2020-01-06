package io.github.jkobejs.zio.google.cloud.oauth2.common.urlencoding

import java.net.URLEncoder

import magnolia._
import simulacrum._

import scala.language.experimental.macros

@typeclass trait UrlEncodedWriter[A] {
  def toUrlEncoded(a: A): String
}

trait UrlEncodedWriterMagnolia {

  type Typeclass[a] = UrlEncodedWriter[a]
  def combine[A](cc: CaseClass[UrlEncodedWriter, A]): UrlEncodedWriter[A] =
    (a: A) =>
      cc.parameters
        .filterNot(p => p.typeclass.toUrlEncoded(p.dereference(a)).isEmpty)
        .map { p =>
          p.label + "=" + p.typeclass.toUrlEncoded(p.dereference(a))
        }
        .toList
        .mkString("&")
  def gen[A]: UrlEncodedWriter[A] = macro Magnolia.gen[A]
}

object UrlEncodedWriter extends UrlEncodedWriterMagnolia {
  implicit def optionA[A: UrlEncodedWriter]: UrlEncodedWriter[Option[A]] = {
    case None        => ""
    case Some(value) => implicitly[UrlEncodedWriter[A]].toUrlEncoded(value)
  }
  implicit val string: UrlEncodedWriter[String]   = s => URLEncoder.encode(s, "UTF-8")
  implicit val long: UrlEncodedWriter[Long]       = _.toString
  implicit val boolean: UrlEncodedWriter[Boolean] = _.toString
}
