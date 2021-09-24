
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.12.13"

// Only the resource is published, independently of Scala version.
ThisBuild / crossPaths := false
ThisBuild / Compile / packageBin / publishArtifact := true  // Do include the resources.
ThisBuild / Compile / packageDoc / publishArtifact := false // There is no documentation.
ThisBuild / Compile / packageSrc / publishArtifact := false // There is no source code.
ThisBuild / Test    / packageBin / publishArtifact := false

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.yaml"       % "snakeyaml" % "1.14"  % Test
)

val ontologies: (String, Seq[String]) = (
  "org.clulab.wm.eidos.english.ontologies", // ontology package
  Seq(
    // The user should set these values.
    "CompositionalOntology_metadata.yml",
    "wm_flat_metadata.yml"
  )
)

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

lazy val root = (project in file("."))
    .dependsOn(common % "test -> test")

lazy val common = project

lazy val extras = project
    .dependsOn(common)

ThisBuild / Test / parallelExecution := false
