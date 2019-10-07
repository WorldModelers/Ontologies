
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

lazy val versioningTask = taskKey[(Option[String], Option[String], Seq[(String, Option[String], Option[String])])]("extract version info from the revision list")
lazy val versioningFiles = taskKey[Seq[String]]("specify which files to version")

versioningFiles := Seq("wm_metadata.yml", "interventions.yml")

versioningTask := {
  val gitRunner: com.typesafe.sbt.git.GitRunner = git.runner.value
  val gitHeadCommitOpt: Option[String] = git.gitHeadCommit.value
  val gitHeadCommitDateOpt: Option[String] = git.gitHeadCommitDate.value
//  val files = Seq("wm_metadata.yml", "interventions.yml") // versioningFiles.value
  val files = versioningFiles.value
//  val files = (versioningFiles in versioningTask).value
  val versions = files.map { file =>
    val gitArgs = Seq("rev-list", "--timestamp", "-1", "master", file)
    val output = gitRunner(gitArgs: _*)(baseDirectory.value, com.typesafe.sbt.git.NullLogger)
    val Array(timestamp, hash) = output.split(" ")

    (file, Some(hash), Some(timestamp))
  }
  (gitHeadCommitOpt, gitHeadCommitDateOpt, versions)
}

def versionIt(gitRunner: com.typesafe.sbt.git.GitRunner, baseDirectory: File, files: Seq[String]): Seq[(String, Option[String], Option[String])] = {
  val versions = files.map { file =>
    val gitArgs = Seq("rev-list", "--timestamp", "-1", "master", file)
    val output = gitRunner(gitArgs: _*)(baseDirectory, com.typesafe.sbt.git.NullLogger)
    val Array(timestamp, hash) = output.split(" ")

    (file, Some(hash), Some(timestamp))
  }

  versions
}

def generateVersions(codebase: File, namespace: String, gitHeadHash: Option[String], gitHeadCommitDate: Option[String],
    ontologies: Seq[(String, Option[String], Option[String])]): Seq[File] = {
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
  val codebase = (sourceManaged in Compile).value
  val namespace = "com.github.worldModelers.ontologies"

//  versioningFiles := Seq("wm_metadata.yml", "interventions.yml")

  val (gitHeadCommitOpt, gitHeadCommitDateOpt, versions) = versioningTask.value

  generateVersions(codebase, namespace, gitHeadCommitOpt, gitHeadCommitDateOpt, versions)
}.taskValue

lazy val root = (project in file("."))
