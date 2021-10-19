import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import sbt.IO
import sbt.util.Logger

import java.io.PrintWriter
import java.io.StringWriter
import java.util.function.Supplier
import scala.util.Try

case class Version(versionOpt: Option[(String, ZonedDateTime)]) {

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

case class Versions(versions: Seq[(String, Version)])

class Versioner(versions: Versions, resourcebase: File, resourceNamespace: String, filenames: Seq[String]) {

  protected def mkFilename(codebase: File, namespace: String): String =
      codebase.getCanonicalPath + "/" + namespace.replace('.', '/') + "/"

  protected def extensionless(filename: String): String =
      if (filename.contains('.')) filename.substring(0, filename.lastIndexOf('.'))
      else filename

  def versionResources(): Seq[File] = {
    val filename = mkFilename(resourcebase, resourceNamespace)
    // Skip the HEAD.
    val files = versions.versions.tail.map { version =>
      val file = new File(extensionless(filename + version._1) + ".properties")
      val properties = version._2.properties

      IO.write(file, properties)
      file
    }
    files
  }
}

object Versioner {

  protected def readVersions(gitRunner: com.typesafe.sbt.git.GitRunner, gitCurrentBranch: String,
      baseDirectory: File, files: Seq[String], logger: Logger, rethrow: Boolean): Versions = {
    val versions = files.map { file =>
      val gitArgs = Seq("rev-list", "--timestamp", "-1", gitCurrentBranch, file)
      // val gitArgs = Seq("log", """--format="%at %H"""", "--max-count=1", gitCurrentBranch, file)

      try {
        val output = gitRunner(gitArgs: _*)(baseDirectory, com.typesafe.sbt.git.NullLogger)
        val Array(timestamp, hash) = output.split(' ')
        val integerTime = Integer.parseInt(timestamp)
        val instant = Instant.ofEpochSecond(integerTime)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)

        logger.info(s"Version($file) = ($hash, $zonedDateTime)")
        (file, Version(Some((hash, zonedDateTime))))
      }
      catch {
        case throwable: Throwable =>
          val supplier: Supplier[String] = new Supplier[String]() {
            override def get(): String = {
              val stringWriter =  new StringWriter()
              val printWriter = new PrintWriter(stringWriter)

              printWriter.println(s"The version for $file couldn't be retrieved.")
              throwable.printStackTrace(printWriter)
              printWriter.close
              stringWriter.toString
            }
          }

          logger.error(supplier)
          if (rethrow)
            throw throwable
          (file, Version(None))
      }
    }

    Versions(versions)
  }

  def apply(gitRunner: com.typesafe.sbt.git.GitRunner, gitCurrentBranch: String,
      baseDirectory: File, resourcebase: File, resourceNamespace: String,
      filenames: Seq[String], logger: Logger, rethrow: Boolean): Versioner = {
    val versions = readVersions(gitRunner, gitCurrentBranch, baseDirectory, "HEAD" +: filenames, logger, rethrow)

    new Versioner(versions, resourcebase, resourceNamespace, filenames)
  }
}
