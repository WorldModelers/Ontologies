package com.github.worldModelers.ontologies

import org.clulab.wm.eidos.utils.Closer.AutoCloser
import org.scalatest._

import java.util.Properties
import java.time.ZonedDateTime

class TestVersioner extends FlatSpec with Matchers {
  // This has to be coordinated with the build file.
  val resourceBase = "/org/clulab/wm/eidos/english/ontologies/"

  protected def extensionless(filename: String): String =
      if (filename.contains('.')) filename.substring(0, filename.lastIndexOf('.'))
      else filename

  behavior of "Versions"

  def getVersionAndDate(filename: String): (String, ZonedDateTime) = {
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


  it should "version" in {
    Seq(
      // This has to be coordinated with the build file.
      "wm_flat_metadata.yml",
      "CompositionalOntology_metadata.yml"
    ).foreach { filename =>
      val (version, zonedDateTime) = getVersionAndDate(filename)

      println(s"$filename")
      println(s"  version: $version")
      println(s"     date: $zonedDateTime")
    }
  }
}
