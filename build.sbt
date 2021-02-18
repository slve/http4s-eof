name := "scala"
parallelExecution := false
scalaVersion := "2.13.3"
scalacOptions ++= Seq("-Wconf:any:warning-verbose")

val sttp = Seq(
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio",
  "com.softwaremill.sttp.client3" %% "core",
  "com.softwaremill.sttp.client3" %% "httpclient-backend-zio"
).map(_ % "3.1.1")

val http4s = Seq(
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-async-http-client",
  "org.http4s" %% "http4s-ember-client",
  "org.http4s" %% "http4s-jetty-client",
  "org.http4s" %% "http4s-okhttp-client"
).map(_ % "0.21.19")

libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-core"               % "2.5.0",
  "dev.zio"       %% "zio"                    % "1.0.4-2",
  "dev.zio"       %% "zio-interop-cats"       % "2.1.4.0",
  "org.http4s"    %% "http4s-jdk-http-client" % "0.3.5",
  "org.scalatest" %% "scalatest"              % "3.2.2",
  "org.typelevel" %% "cats-core"              % "2.4.1",
  "org.typelevel" %% "cats-effect"            % "2.3.1"
) ++ sttp ++ http4s
