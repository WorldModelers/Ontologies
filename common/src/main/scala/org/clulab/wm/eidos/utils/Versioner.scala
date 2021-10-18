package org.clulab.wm.eidos.utils

import java.time.ZonedDateTime
import java.util.Properties

object Versioner {

  protected def extensionless(filename: String): String =
    if (filename.contains('.')) filename.substring(0, filename.lastIndexOf('.'))
    else filename

  def getVersionAndDate(resourceBase: String, filename: String): (String, ZonedDateTime) = {
    val path = resourceBase + extensionless(filename) + ".properties"
    val properties = this.getClass.getResourceAsStream(path).autoClose { inputStream =>
      val properties = new Properties

      properties.load(inputStream)
      properties
    }
    val version = properties.getProperty("hash")
    val date = properties.getProperty("date")
    val zonedDateTime = ZonedDateTime.parse(date)

    (version, zonedDateTime)
  }
}
