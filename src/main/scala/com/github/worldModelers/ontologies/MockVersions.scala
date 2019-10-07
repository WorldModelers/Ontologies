/* Code similar to this is automatically generated during project compilation. */

package com.github.worldModelers.ontologies

import java.time.ZonedDateTime

case class MockVersion(filename: String, startHash: String, startDate: ZonedDateTime)

object MockVersions {
  // These first values apply to the entire repository.
  val endHash: String = "2db42aa7c62d9b3b4cf99a08ec393121e53ce5cd"
  val endDate: ZonedDateTime = ZonedDateTime.parse("2019-10-04T20:49:00Z")

  val versions: Map[String, MockVersion] = Map(
    "wm" -> MockVersion("wm_metadata.yml", "8c3d191c837e973a9ebfacaa78d3a96ab1701981", ZonedDateTime.parse("2019-10-04T17:18:20Z")),
    "interventions" -> MockVersion("interventions_metadata.yml", "9fc7e0860cf54b7b54378bf3b73efe6e68e4e10b", ZonedDateTime.parse("2019-07-09T12:49:08Z"))
  )
}