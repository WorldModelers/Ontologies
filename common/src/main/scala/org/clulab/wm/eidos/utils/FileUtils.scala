package org.clulab.wm.eidos.utils

import org.clulab.wm.eidos.utils.Closer.AutoCloser

import scala.io.Source

import java.io.File
import java.nio.charset.StandardCharsets

object FileUtils {
  val utf8: String = StandardCharsets.UTF_8.toString

  def getTextFromFile(path: String): String = getTextFromFile(new File(path))

  def getTextFromFile(file: File): String = {
    Source.fromFile(file, utf8).autoClose { source =>
      source.mkString
    }
  }
}
