name := "AKKASTREAMS"

version := "1.0"

scalaVersion := "2.11.8"

fork in Test := true
(parallelExecution in Test) := false

libraryDependencies ++= {
  val akkaV       = "2.4.11"
  val scalaTestV  = "2.2.6"
  Seq(
    "com.typesafe.akka"   %%  "akka-actor"                           % akkaV,
    "com.typesafe.akka"   %%  "akka-stream"             		         % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"                         % akkaV,
    "org.scalatest"       %%  "scalatest"                            % scalaTestV % "test"
  )
}
    