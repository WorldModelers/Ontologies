package org.clulab.linnaeus.model.fmt2.io

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

import org.clulab.linnaeus.model.util.Closer.AutoCloser
import org.clulab.linnaeus.model.util.FileUtil

abstract class GraphReader {

  def read(bufferedInputStream: BufferedInputStream): Unit

  /**
    * According to https://bitbucket.org/asomov/snakeyaml/wiki/Documentation#markdown-header-loading-yaml,
    * "Yaml.load() accepts a String or an InputStream object. Yaml.load(InputStream stream) detects the encoding
    * by checking the BOM (byte order mark) sequence at the beginning of the stream. If no BOM is present, the utf-8
    * encoding is assumed."
    */
  def read(inputStream: InputStream): Unit =
    read(FileUtil.newBufferedInputStream(inputStream))

  def readFromString(string: String): Unit = {
    val inputStream = new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8))
    read(inputStream)
  }

  def readFromFile(path: String): Unit = {
    FileUtil.newInputStream(path).autoClose { inputStream =>
      read(inputStream)
    }
  }

  def readFromResource(path: String): Unit = {
    FileUtil.newResourceInputStream(path).autoClose { inputStream =>
      read(inputStream)
    }
  }

  def read(path: String): Unit = readFromFile(path)
}
