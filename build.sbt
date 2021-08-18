
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.12.13"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.yaml"       % "snakeyaml" % "1.14"  % Test
)

val ontologies: (String, String, Seq[String]) = (
  "com.github.worldModelers.ontologies", // version package
  "org.clulab.wm.eidos.english.ontologies", // ontology package
  Seq(
    // The user should set these values.
    "CompositionalOntology_metadata.yml",
    "wm_flat_metadata.yml"
  )
)

// This copies the files to their correct places for compilation and packaging.
// In the meantime they can be placed where it's easy to edit them.
mappings in (Compile, packageBin) ++= {
  val (_, resourceNamespace, filenames) = ontologies
  val basedir = resourceNamespace.replace('.', '/') + "/"

  filenames.map { filename =>
    file(filename) -> (basedir + filename)
  }
}

lazy val versionTask = Def.task {
  val (codeNamespace, resourceNamespace, filenames) = ontologies
  val versioner = Versioner(
    git.runner.value, git.gitCurrentBranch.value,
    baseDirectory.value, (sourceManaged in Compile).value, (resourceManaged in Compile).value,
    codeNamespace, resourceNamespace,
    // Turn off rethrow to complete even in the presence of an error.
    filenames, streams.value.log, rethrow = true
  )

  versioner
}

sourceGenerators in Compile += Def.task {
  versionTask.value.versionCode()
}.taskValue

resourceGenerators in Compile += Def.task {
  versionTask.value.versionResources()
}.taskValue

lazy val root = project in file(".")

ThisBuild / Test / parallelExecution := false
