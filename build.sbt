
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.11.12" // "2.12.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.yaml" % "snakeyaml" % "1.14"
)

mappings in (Compile, packageBin) ++= Seq(
  file("CompositionalOntology_v2.1_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/CompositionalOntology_v2.1_metadata.yml",
  file("wm_flat_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/wm_flat_metadata.yml"
)

sourceGenerators in Compile += Def.task {
  import Versioner._
  // These values need to be collected in a task in order have them forwarded to Scala functions.
  val versioner = Versioner(git.runner.value, git.gitCurrentBranch.value, baseDirectory.value,
      (sourceManaged in Compile).value, (resourceManaged in Compile).value)

  // The user should set these values.
  val namespace = "com.github.worldModelers.ontologies"
  val files = Seq(
    "CompositionalOntology_v2.1_metadata.yml",
    "wm_flat_metadata.yml"
  )

  // This reads and codes the versions.
  versioner.versionCode(namespace, files)
}.taskValue

resourceGenerators in Compile += Def.task {
  import Versioner._
  // These values need to be collected in a task in order have them forwarded to Scala functions.
  val versioner = Versioner(git.runner.value, git.gitCurrentBranch.value, baseDirectory.value,
    (sourceManaged in Compile).value, (resourceManaged in Compile).value)

  // The user should set these values.
  val namespace = "com.github.worldModelers.ontologies"
  val files = Seq(
    "CompositionalOntology_v2.1_metadata.yml",
    "wm_flat_metadata.yml"
  )

  // This reads and codes the versions.
  versioner.versionResources(namespace, files)
}.taskValue

lazy val root = project in file(".")
