
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

def readVersionsBase(gitRunner: com.typesafe.sbt.git.GitRunner, baseDirectory: File)(files: Seq[String]): Seq[(String, Option[String], Option[String])] = {
  val versions = files.map { file =>
    val gitArgs = Seq("rev-list", "--timestamp", "-1", "master", file)
    val output = gitRunner(gitArgs: _*)(baseDirectory, com.typesafe.sbt.git.NullLogger)
    val Array(timestamp, hash) = output.split(" ")

    (file, Some(hash), Some(timestamp))
  }

  versions
}

def codeVersionsBase(codebase: File, gitHeadHash: Option[String], gitHeadCommitDate: Option[String])(
    namespace: String, ontologies: Seq[(String, Option[String], Option[String])]): Seq[File] = {
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
  val readVersions = readVersionsBase(git.runner.value, baseDirectory.value) _
  val codeVersions = codeVersionsBase((sourceManaged in Compile).value, git.gitHeadCommit.value, git.gitHeadCommitDate.value) _

  // The user should set these values.
  val namespace = "com.github.worldModelers.ontologies"
  val files = Seq("wm_metadata.yml", "interventions.yml")

  val versions = readVersions(files)
  codeVersions(namespace, versions)
}.taskValue

lazy val root = project in file(".")
