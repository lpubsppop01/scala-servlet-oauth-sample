name := "scala-servlet-oauth-sample"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

fork in run := true

seq(webSettings :_*)

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5" % "provided",
  "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
)

artifactName := { (config: String, module: ModuleID, artifact: Artifact) =>
  "root." + artifact.extension
}
