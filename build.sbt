name := "scala"
scalaVersion := "2.13.3"
scalacOptions ++= Seq("-Wconf:any:warning-verbose")

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-core"               % "2.5.0",
  "org.http4s"    %% "http4s-jdk-http-client" % "0.3.5",
  "org.scalatest" %% "scalatest"              % "3.0.8",
  "org.typelevel" %% "cats-core"              % "2.4.1",
  "org.typelevel" %% "cats-effect"            % "2.3.1"
) ++ Seq(
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
).map(_ % "0.21.18")
