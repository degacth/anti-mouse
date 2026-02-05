val scala3Version = "3.8.1"
val zioVersion = "2.1.24"
val zioConfig = "4.0.6"


lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging, BuildInfoPlugin)
  .settings(
    name := "anti-mouse",
    version := "0.0.3",
    maintainer := "degacth@yandex.ru",
    scalaVersion := scala3Version,
    fork := true,
    connectInput := true,

    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,

      "dev.zio" %% "zio-config" % zioConfig,
      "dev.zio" %% "zio-config-magnolia" % zioConfig,
      "dev.zio" %% "zio-config-typesafe" % zioConfig,
      "dev.zio" %% "zio-config-refined" % zioConfig,

      "com.github.kwhat" % "jnativehook" % "2.2.2",

      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
    ),

    javaOptions ++= Seq(
      "--enable-native-access=ALL-UNNAMED",
    ),
  )
