package com.github.worldModelers.ontologies

import org.clulab.wm.eidos.utils.Versioner
import org.scalatest._

class TestVersioner extends FlatSpec with Matchers {
  // This has to be coordinated with the build file.
  val resourceBase = "/org/clulab/wm/eidos/english/ontologies/"

  behavior of "Versions"

  it should "version" in {
    Seq(
      // This has to be coordinated with the build file.
      "wm_flat_metadata.yml",
      "CompositionalOntology_metadata.yml"
    ).foreach { filename =>
      val (version, zonedDateTime) = Versioner.getVersionAndDate(resourceBase, filename)

      println(s"$filename")
      println(s"  version: $version")
      println(s"     date: $zonedDateTime")
    }
  }
}
