
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.yaml" % "snakeyaml" % "1.14"
)

mappings in (Compile, packageBin) ++= Seq(
  file("wm_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/wm_metadata.yml",
  file("wm_with_flattened_interventions_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/wm_with_flattened_interventions_metadata.yml",
  file("wm_compositional_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/wm_compositional_metadata.yml"
)

sourceGenerators in Compile += Def.task {
  import Versioner._
  // These values need to be collected in a task in order have them forwarded to Scala functions.
  val versioner = Versioner(git.runner.value, git.gitCurrentBranch.value, baseDirectory.value, (sourceManaged in Compile).value)

  // The user should set these values.
  val namespace = "com.github.worldModelers.ontologies"
  val files = Seq(
    "wm_metadata.yml",
    "wm_with_flattened_interventions_metadata.yml",
    "wm_compositional_metadata.yml"
  )

  // This reads and codes the versions.
  versioner.version(namespace, files)
}.taskValue

lazy val root = project in file(".")
