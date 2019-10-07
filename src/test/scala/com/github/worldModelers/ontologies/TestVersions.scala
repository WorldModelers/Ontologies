package com.github.worldModelers.ontologies

// Switch these back and forth to test code generation
import com.github.worldModelers.ontologies.{MockVersions => Versions, MockVersion => Version }
//import com.github.worldModelers.ontologies.{Versions => Versions, Version => Version }
import java.time.ZonedDateTime

import org.scalatest._

class TestVersions extends FlatSpec with Matchers {
  val now = ZonedDateTime.now

  behavior of "versions"

  it should "provide global values"

  Versions.endHash.nonEmpty should be (true)
  Versions.endDate.isBefore(now) should be (true)

  def test(name: String): Unit = {
    it should "should document version of " + name in {
      Versions.versions.contains(name) should be (true)

      val version: Version = Versions.versions(name)

      version.filename.nonEmpty should be (true)
      version.startHash.nonEmpty should be (true)
      (version.startDate.isBefore(Versions.endDate) || version.startDate.isEqual(Versions.endDate)) should be (true)
    }
  }

  Seq(
    "wm"
  ).foreach(name => test(name))
}
