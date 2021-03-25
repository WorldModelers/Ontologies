
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.11.12" // "2.12.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.yaml" % "snakeyaml" % "1.14"
)

// This copies the files to their correct places for compilation and packaging.
// In the meantime they can be placed where it's easy to edit them.
mappings in (Compile, packageBin) ++= Seq(
  file("CompositionalOntology_v2.1_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/CompositionalOntology_v2.1_metadata.yml",
  file("wm_flat_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/wm_flat_metadata.yml"
)

//lazy val versionTask = taskKey[Versioner]("Retrieve version numbers of ontologies.")

lazy val versionTaskImpl = Def.task {
  import Versioner._
  // The user should set these values.  They should be coordinated with the mappings above.
  val namespace = "com.github.worldModelers.ontologies"
  val files = Seq(
    "CompositionalOntology_v2.1_metadata.yml",
    "wm_flat_metadata.yml"
  )
  val versioner = Versioner(
    git.runner.value, git.gitCurrentBranch.value, baseDirectory.value,
    (sourceManaged in Compile).value, (resourceManaged in Compile).value,
    namespace, files
  )

  versioner
}

sourceGenerators in Compile += Def.task {
  versionTaskImpl.value.versionCode()
}.taskValue

resourceGenerators in Compile += Def.task {
  versionTaskImpl.value.versionResources()
}.taskValue

lazy val root = project in file(".")
