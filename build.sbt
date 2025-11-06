val scala3Version = "3.7.3"
val zioVersion = "2.1.22"

lazy val root = project
  .in(file("."))
  .settings(
    name := "anti-mouse",
    version := "0.0.0",
    scalaVersion := scala3Version,
    fork := true,
    connectInput := true,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "com.github.kwhat" % "jnativehook" % "2.2.2",

      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
    )
  )
