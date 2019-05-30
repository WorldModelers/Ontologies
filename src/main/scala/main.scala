import java.nio.charset.StandardCharsets

import scala.io.BufferedSource
import scala.io.Source

object Main extends App {
  val utf8: String = StandardCharsets.UTF_8.toString

  println("Hello, world!")

  def sourceFromResource(path: String): BufferedSource = {
    val url = Option(Main.getClass.getResource(path))
        .getOrElse(throw new Exception(s"File not found: $path"))

    println("Sourcing resource " + url.getPath)
    Source.fromURL(url, utf8)
  }

  sourceFromResource("/org/clulab/wm/eidos/english/ontologies/wm_metadata.yml")
      .getLines
      .foreach(println(_))

}