import sbt._

object Settings {
  lazy val deeplearning4j = RootProject(uri("ssh://git@github.com:SkymindIO/deeplearning4j.git"))
}

object EngineBuild extends Build {
  lazy val root = Project(
    id = "template-scala-parallel-word2vec-test",
    base = file(".")
  ).dependsOn(
      Settings.deeplearning4j
    )
}