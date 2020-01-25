package io.github.jkobejs.zio.google.cloud.oauth2.utils

import java.awt.Desktop

import zio.Task

object Browser {

  def open(url: String): Task[Unit] = Task.effect(Desktop.getDesktop.browse(new java.net.URI(url)))
}
