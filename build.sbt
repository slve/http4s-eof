name := "scala"
parallelExecution := false
scalaVersion := "2.13.3"
scalacOptions ++= Seq("-Wconf:any:warning-verbose")

libraryDependencies ++= Seq(
  "co.fs2"                        %% "fs2-core"               % "2.5.0",
  "com.softwaremill.sttp.client3" %% "core"                   % "3.1.1",
  "org.http4s"                    %% "http4s-jdk-http-client" % "0.3.5",
  "org.scalatest"                 %% "scalatest"              % "3.2.2",
  "org.typelevel"                 %% "cats-core"              % "2.4.1",
  "org.typelevel"                 %% "cats-effect"            % "2.3.1"
) ++ Seq(
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-ember-client"
).map(_ % "0.21.19")
