
name := "Ontologies"

organization := "WorldModelers"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.yaml" % "snakeyaml" % "1.14"
)

mappings in (Compile, packageBin) += {
  file("wm_metadata.yml") -> "org/clulab/wm/eidos/english/ontologies/wm_metadata.yml"
}

sourceGenerators in Compile += Def.task {
  import Versioner._
  val versioner = Versioner(
    // These values need to be collected in a task in order to forward them to Scala functions.
    git.runner.value, baseDirectory.value,
    (sourceManaged in Compile).value, git.gitHeadCommit.value, git.gitHeadCommitDate.value
  )

  // The user should set these values.
  val namespace = "com.github.worldModelers.ontologies"
  val files = Seq("wm_metadata.yml", "interventions.yml")

  // This reads and codes the versions.
  versioner.version(namespace,files)
}.taskValue

lazy val root = project in file(".")
