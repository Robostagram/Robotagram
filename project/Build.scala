import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "Hermione"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      jdbc,
      anorm,
      "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
    )

  // taken from : https://github.com/playframework/Play20/wiki/Tips
  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
    def customLessEntryPoints(base: File): PathFinder = (
      (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
      (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++
      (base / "app" / "assets" / "stylesheets" * "*.less")
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
      // compile bootstrap.less, the entry point for Bootstrap LESS CSS
      lessEntryPoints <<= baseDirectory(customLessEntryPoints)
    )

}
