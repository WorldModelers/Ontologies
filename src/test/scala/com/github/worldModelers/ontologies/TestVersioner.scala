package com.github.worldModelers.ontologies

// Switch these back and forth to test code generation
//import com.github.worldModelers.ontologies.{MockVersions => TestVersions, MockVersion => TestVersion }
import com.github.worldModelers.ontologies.{Versions => TestVersions, Version => TestVersion }
import java.time.ZonedDateTime

import org.scalatest._

class TestVersioner extends FlatSpec with Matchers {
  val now = ZonedDateTime.now

  behavior of "Versions"

  def test(file: String, version: Option[TestVersion], expirationDate: ZonedDateTime): Unit = {
    it should "document version of " + file in {

      version.nonEmpty should be (true)
      version.get.commit.nonEmpty should be (true)

      val versionDate = version.get.date

      (versionDate.isBefore(expirationDate) || versionDate.isEqual(expirationDate)) should be (true)
      println(file + ": " + version)
    }
  }

  test("HEAD", TestVersions.version, now)

  TestVersions.versions.nonEmpty should be (true)

  TestVersions.versions.foreach { case (file, version) =>
    test(file, version, TestVersions.version.map { _.date }.getOrElse(now))
  }
}
