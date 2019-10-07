
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

lazy val versioningTask = taskKey[Seq[(String, String, String)]]("extract version info from the revision list")

versioningTask := {
  val runner: com.typesafe.sbt.git.GitRunner = git.runner.value
  val dir = baseDirectory.value
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

def generateVersions(gitrunner: com.typesafe.sbt.git.GitRunner, codebase: File, namespace: String, ontologies: Map[String, String]): Seq[File] = {
  println(s"Generating resources in $codebase at $namespace for $ontologies")
  val filename = codebase.getCanonicalPath + "/" + namespace.replace('.', '/') + "/Versions.scala"
  val version = "Once upon a time"
  val file = new File(filename)
  val gitHeadHash = git.gitHeadCommit.value.get
  val gitHeadCommitDate = git.gitHeadCommitDate.value.get
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
    |  val endHash: String = "$gitHeadHash"
    |  val endDate: ZonedDateTime = ZonedDateTime.parse($gitHeadCommitDate)
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
  val ontologies = Map {
    "wm" -> "wm_metadata.yml"
  }

  generateVersions(git.runner.value, codebase, namespace, ontologies)
}.taskValue

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoPackage := "worldmodelers.ontologies",
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoKeys := Seq[BuildInfoKey](
      name, version, scalaVersion, sbtVersion, libraryDependencies, scalacOptions,
      "gitCurrentBranch" -> { git.gitCurrentBranch.value },
      "gitHeadCommit" -> { git.gitHeadCommit.value.getOrElse("") },
      "gitHeadCommitDate" -> { git.gitHeadCommitDate.value.getOrElse("") },
      "gitUncommittedChanges" -> { git.gitUncommittedChanges.value }
    )
  )
