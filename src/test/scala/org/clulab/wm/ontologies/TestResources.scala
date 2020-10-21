package org.clulab.wm.ontologies

import org.clulab.wm.eidos.utils.Closer.AutoCloser
import org.clulab.wm.eidos.utils.Sourcer

import org.scalatest._

class TestResources extends FlatSpec with Matchers {
  
  behavior of "resources"

  def test(path: String): Unit = {
    it should "not have any strange characters in " + path in {
      val count = Sourcer.sourceFromFile(path).autoClose { source =>
        source.getLines().zipWithIndex.foldRight(0) { (lineAndLineNo, sum) =>
          val line = lineAndLineNo._1
          val lineNo = lineAndLineNo._2
          val badCharAndIndex = line.zipWithIndex.filter { case (c: Char, _: Int) =>
            (c < 32 || 127 < c) && c != '\r' && c != '\n' && c != '\t'
          }
          badCharAndIndex.foreach { case (c: Char, index: Int) =>
            println(s"Line ${lineNo + 1}: '$c' found at index $index.")
          }
          sum + badCharAndIndex.size
        }
      }
      count should be (0)
    }
  }

  Seq(
    "./CompositionalOntology_v2.1_metadata.yml",
    "./wm_flat_metadata.yml"
  ).foreach(test)
}
