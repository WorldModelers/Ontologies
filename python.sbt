// See https://stackoverflow.com/questions/30205003/using-sbt-to-manage-projects-that-contain-both-scala-and-python

import sbt.io.IO
import scala.sys.process.Process

val testPythonTask = TaskKey[Unit]("testPython", "Run Python tests")

testPythonTask := {
  val taskStreams: TaskStreams = streams.value
  taskStreams.log.info("Starting testPythonTask...")

  val command = "pytest"
  val mainDir = new File("./scripts")
  val workDir = new File("./target/python")
  val testDir = new File("./target/python/tests")

  taskStreams.log.info(s"Deleting workDir $workDir...")
  IO.delete(workDir)
  taskStreams.log.info(s"Copying mainDir $mainDir...")
  IO.copyDirectory(mainDir, workDir)
  taskStreams.log.info(s"Executing command $command...")
  // The PYTHONPATH needs to point from testDir to workDir.
  val result = Process(command, testDir, "PYTHONPATH" -> "..") ! taskStreams.log
  taskStreams.log.info("Stopping testPythonTask...")

  val message = s"Result of testPythonTask was $result."
  taskStreams.log.info(message)
  if (result != 0)
    sys.error(message)
}
