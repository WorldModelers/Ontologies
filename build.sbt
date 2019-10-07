
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

// Turn this into a case class
// Learn how to serialize it

lazy val versioningTask = taskKey[Seq[(String, String, String)]]("extract version info from the revision list")
val versioningFiles = settingKey[Seq[String]]("Specifies which files to version.")

versioningTask := {
  val runner: com.typesafe.sbt.git.GitRunner = git.runner.value
  val dir = baseDirectory.value
  // needs to get some values from outside!
  val files = Seq("wm_metadata.yml", "interventions.yml")
  
  //  println(buildInfoKeys.value) // does show proper information
  //  buildInfoKeys := buildInfoKeys.value + "keith" -> { "brad" }
  //  println(buildInfoKeys.value) // does show proper information
  files.map { file =>
    val gitArgs = Seq("rev-list", "--timestamp", "-1", "master", file)
    val output = runner(gitArgs: _*)(dir, com.typesafe.sbt.git.NullLogger)
    val Array(timestamp, hash) = output.split(" ")

    (file, hash, timestamp)
  }
}

def generateVersions(gitRunner: com.typesafe.sbt.git.GitRunner, gitHeadHash: Option[String], gitHeadCommitDate: Option[String],
    codebase: File, namespace: String, ontologies: Map[String, String]): Seq[File] = {
  println(s"Generating resources in $codebase at $namespace for $ontologies")
  val filename = codebase.getCanonicalPath + "/" + namespace.replace('.', '/') + "/Versions.scala"
  val file = new File(filename)
  val versions = """"a" -> "b""""

  val code = s"""
    |/* This code is automatically generated during project compilation. */
    |
    |package $namespace
    |
    |import java.time.ZonedDateTime
    |
    |case class Version(filename: String, startHash: String, startDate: ZonedDateTime)
    |
    |object Versions {
    |  // These first values apply to the entire repository.
    |  val endHash: String = "${gitHeadHash.get}"
    |  val endDate: ZonedDateTime = ZonedDateTime.parse("${gitHeadCommitDate.get}")
    |
    |  val versions: Map[String, String] = Map(
    |    $versions
    |  )
    |}
    |""".stripMargin.trim + "\n"

  IO.write(file, code)
  Seq(file)
}

sourceGenerators in Compile += Def.task {
  // Capture git values in a task so they can be used elsewhere.
  def generateGitVersions(codebase: File, namespace: String, ontologies: Map[String, String]) =
      generateVersions(git.runner.value, git.gitHeadCommit.value, git.gitHeadCommitDate.value,
      codebase, namespace, ontologies)

  val codebase = (sourceManaged in Compile).value
  val namespace = "com.github.worldModelers.ontologies"
  val ontologies = Map {
    "wm" -> "wm_metadata.yml"
  }

  generateGitVersions(codebase, namespace, ontologies)
}.taskValue

lazy val root = (project in file("."))
