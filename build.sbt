
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.12.13"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.yaml"       % "snakeyaml" % "1.14"  % Test
)

lazy val root = (project in file("."))
    .dependsOn(common % "test -> test")

lazy val common = project

lazy val extras = project
    .dependsOn(common)

val ontologies: (String, Seq[String]) = (
  "org.clulab.wm.eidos.english.ontologies", // ontology package
  Seq(
    // The user should set these values.
    "CompositionalOntology_metadata.yml",
    "wm_flat_metadata.yml"
  )
)

// Only the resource is published, independently of Scala version.
ThisBuild / crossPaths := false
ThisBuild / Compile / packageBin / publishArtifact := true  // Do include the resources.
ThisBuild / Compile / packageDoc / publishArtifact := false // There is no documentation.
ThisBuild / Compile / packageSrc / publishArtifact := false // There is no source code.
ThisBuild / Test    / packageBin / publishArtifact := false

// This copies the files to their correct places for packaging.
// In the meantime they can be placed where it's easy to edit them.
Compile / packageBin / mappings ++= {
  val (resourceNamespace, filenames) = ontologies
  val basedir = resourceNamespace.replace('.', '/') + "/"

  filenames.map { filename =>
    file(filename) -> (basedir + filename)
  }
}

lazy val versionTask = Def.task {
  val (resourceNamespace, filenames) = ontologies
  val versioner = Versioner(
    git.runner.value, git.gitCurrentBranch.value,
    baseDirectory.value, (Compile / resourceManaged).value,
    resourceNamespace,
    // Turn off rethrow to complete even in the presence of an error.
    filenames, streams.value.log, rethrow = true
  )

  versioner
}

Compile / resourceGenerators += Def.task {
  versionTask.value.versionResources()
}.taskValue

ThisBuild / Test / parallelExecution := false

import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}
import scala.xml.transform.{RewriteRule, RuleTransformer}

// See https://stackoverflow.com/questions/41670018/how-to-prevent-sbt-to-include-test-dependencies-into-the-pom
// Skip dependency elements with a scope.
pomPostProcess := { (node: XmlNode) =>
  new RuleTransformer(new RewriteRule {
    override def transform(node: XmlNode): XmlNodeSeq = node match {
      case e: Elem if e.label == "dependency" => Text("")
      case _ => node
    }
  }).transform(node).head
}
