addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0") // -74e03afef8cbbc0b360fc6fa899cca9ebacc5ce9-SNAPSHOT") // 1.0.0")

libraryDependencies ++= Seq(
  // This dependency is somehow not accounted for the plugin above.
  // It has in turn its own problems...
//  "org.eclipse.jgit" % "org.eclipse.jgit.pgm" %  "4.9.0.201710071750-r"
//      exclude("javax.jms", "jms") exclude("com.sun.jdmk", "jmxtools") exclude("com.sun.jmx", "jmxri")
)
