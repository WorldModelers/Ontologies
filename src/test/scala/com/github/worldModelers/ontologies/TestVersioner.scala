package com.github.worldModelers.ontologies

import java.time.ZonedDateTime
import java.util.Properties

// Switch these back and forth to test code generation
//import com.github.worldModelers.ontologies.{MockVersions => TestVersions, MockVersion => TestVersion }
import com.github.worldModelers.ontologies.{Version => TestVersion, Versions => TestVersions}
import org.clulab.wm.eidos.utils.Closer.AutoCloser

import org.scalatest._

class TestVersioner extends FlatSpec with Matchers {
  val now = ZonedDateTime.now
  // This has to be coordinated with the build file.
  val resourceBase = "/org/clulab/wm/eidos/english/ontologies/"

  protected def extensionless(filename: String): String =
      if (filename.contains('.')) filename.substring(0, filename.lastIndexOf('.'))
      else filename

  behavior of "Versions"

  def test(file: String, versionOpt: Option[TestVersion], expirationDate: ZonedDateTime): Unit = {
    it should "document version of " + file in {

      versionOpt.nonEmpty should be (true)

      val version = versionOpt.get
      version.commit.nonEmpty should be (true)

      val versionDate = version.date
      (versionDate.isBefore(expirationDate) || versionDate.isEqual(expirationDate)) should be (true)
      println(file + ": " + version)

      if (file != "HEAD") {
        val resourceName = extensionless(resourceBase + file) + ".properties"
        val properties = getClass.getResourceAsStream(resourceName).autoClose { stream =>
          val properties = new Properties()

          properties.load(stream)
          properties
        }

        val hash = properties.getProperty("hash")
        val date = properties.getProperty("date")

        hash should be (version.commit)
        date should be (versionDate.toString)
      }
    }
  }

  test("HEAD", TestVersions.version, now)

  TestVersions.versions.nonEmpty should be (true)

  TestVersions.versions.foreach { case (file, version) =>
    test(file, version, TestVersions.version.map { _.date }.getOrElse(now))
  }
}
