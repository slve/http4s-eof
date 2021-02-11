name := "scala"
scalaVersion := "2.13.3"
scalacOptions ++= Seq("-Wconf:any:warning-verbose")

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-core"            % "2.5.0",
  "org.http4s"    %% "http4s-blaze-client" % "0.21.18",
  "org.scalatest" %% "scalatest"           % "3.0.8",
  "org.typelevel" %% "cats-core"           % "2.4.1",
  "org.typelevel" %% "cats-effect"         % "2.4.1"
)
