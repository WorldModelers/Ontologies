import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

import sbt.IO

import scala.util.Try

case class Version(versionOpt: Option[(String, ZonedDateTime)]) {

  def code: String = {
    val text = versionOpt match {
      case Some((hash: String, date: ZonedDateTime)) => s"""Some(Version("$hash", ZonedDateTime.parse("$date")))"""
      case None => "None"
    }

    text
  }

  def properties: String = {
    val text = versionOpt.map { version =>
      s"""
         |hash = ${version._1}
         |date = ${version._2}
         |""".stripMargin.trim + "\n"
    }.getOrElse("")

    text
  }
}

object Version {

  // The date is in ISO 8601 format.
  def apply(hash: Option[String], date: Option[String]): Version = {
    if (hash.isDefined && date.isDefined) {
      Try {
        Version(Some(hash.get, ZonedDateTime.parse(date.get)))
      }.getOrElse(Version(None))
    }
    else
      Version(None)
  }
}

case class Versions(versions: Seq[(String, Version)]) {

  def code: String = {
    val text = versions.map { case (file, version) =>
      s""""$file" -> ${version.code}"""
    }.mkString(",\n    ")

    text
  }
}

class Versioner(versions: Versions, codebase: File, resourcebase: File, namespace: String, files: Seq[String]) {

  protected def mkFilename(codebase: File, namespace: String): String =
      codebase.getCanonicalPath + "/" + namespace.replace('.', '/') + "/"

  def versionCode(): Seq[File] = {
    val filename = mkFilename(codebase, namespace) + "Versions.scala"
    val file = new File(filename)
    val versionCode = versions.versions.head._2.code // This should be the HEAD
    val versionsCode = Versions(versions.versions.tail).code

    val code = s"""
      |/* This code is automatically generated during project compilation. */
      |
      |package $namespace
      |
      |import java.time.ZonedDateTime
      |
      |case class Version(commit: String, date: ZonedDateTime)
      |
      |object Versions {
      |  // This first value applies to the entire repository.
      |  val version: Option[Version] = $versionCode
      |
      |  // These values are for individual files.
      |  val versions: Map[String, Option[Version]] = Map(
      |    $versionsCode
      |  )
      |}
      |""".stripMargin.trim + "\n"

    IO.write(file, code)
    Seq(file)
  }

  def versionResources(): Seq[File] = {
    val filename = mkFilename(resourcebase, namespace)
    println("Running codeProperties")
    println(s"filename is $filename")
    // Skip the HEAD.
    val files = versions.versions.tail.map { version =>
      val file = new File(filename + version._1 + ".properties")
      println(s"One file is ${file.getAbsolutePath}")
      val properties = version._2.properties

      IO.write(file, properties)
      file
    }
    files
  }
}

object Versioner {

  protected def readVersions(gitRunner: com.typesafe.sbt.git.GitRunner, gitCurrentBranch: String, baseDirectory: File,
      files: Seq[String]): Versions = {
    val versions = files.map { file =>
      val gitArgs = Seq("rev-list", "--timestamp", "-1", gitCurrentBranch, file)
      // val gitArgs = Seq("log", """--format="%at %H"""", "--max-count=1", gitCurrentBranch, file)

      try {
        val output = gitRunner(gitArgs: _*)(baseDirectory, com.typesafe.sbt.git.NullLogger)
        val Array(timestamp, hash) = output.split(' ')
        val integerTime = Integer.parseInt(timestamp)
        val instant = Instant.ofEpochSecond(integerTime)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

        (file, Version(Some((hash, zonedDateTime))))
      }
      catch {
        case throwable: Throwable =>
          println(s"Warning: Couldn't get version for $file.")
          throwable.printStackTrace()
          (file, Version(None))
      }
    }

    Versions(versions)
  }

  def apply(gitRunner: com.typesafe.sbt.git.GitRunner, gitCurrentBranch: String, baseDirectory: File,
      codebase: File, resourcebase: File, namespace: String, files: Seq[String]): Versioner = {
    val versions = readVersions(gitRunner, gitCurrentBranch, baseDirectory, "HEAD" +: files)

    new Versioner(versions, codebase, resourcebase, namespace, files)
  }
}
