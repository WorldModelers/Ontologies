package org.clulab.linnaeus.model.util

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

object FileUtil {
  val UTF8: String = StandardCharsets.UTF_8.toString

  def newPrintWriter(filename: String): PrintWriter = {
    newPrintWriter(
      new FileOutputStream(
        new File(filename)
      )
    )
  }

  def newPrintWriter(outputStream: OutputStream): PrintWriter = {
    new PrintWriter(
      new OutputStreamWriter(
        new BufferedOutputStream(outputStream),
        StandardCharsets.UTF_8.toString
      )
    )
  }

  def newBufferedInputStream(inputStream: InputStream): BufferedInputStream =
      new BufferedInputStream(inputStream)

  def newFileInputStream(filename: String): InputStream = {
    new FileInputStream(
      new File(filename)
    )
  }

  def newResourceInputStream(resourcename: String): InputStream = {
    val classLoader1 = FileUtil.getClass.getClassLoader
    val classLoader2 = Thread.currentThread().getContextClassLoader()

    classLoader1.getResourceAsStream(resourcename)
  }

  def newInputStream(filename: String): InputStream = newFileInputStream(filename)

  def newBufferedInputStream(filename: String): BufferedInputStream = {
    newBufferedInputStream(
      newInputStream(filename)
    )
  }
}
