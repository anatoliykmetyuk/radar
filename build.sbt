val ScalaVer = "2.12.6"

// Core
val Cats          = "1.1.0"
val CatsEffect    = "0.10.1"
val KindProjector = "0.9.7"
val Shapeless     = "2.3.3"

// Specialized
val FS2    = "0.10.5"
val Doobie = "0.6.0-M1"
val Circe  = "0.9.3"
val Http4s = "0.18.13"
val Sttp   = "1.2.1"

// Tests
val ScalaTest  = "3.0.5"
val ScalaCheck = "1.14.0"

// Java Utility
val CommonsIO      = "2.6"
val Jsoup          = "1.11.3"
val Selenium       = "3.13.0"
val ApacheCodec    = "1.11"
val SLF4J          = "1.8.0-beta2"
val PostgresDriver = "42.2.2"

lazy val commonSettings = Seq(
  name    := "radar"
, version := "0.1.0"
, scalaVersion := ScalaVer
, libraryDependencies ++= Seq(
    // Core
    "org.typelevel"  %% "cats-core"   % Cats
  , "org.typelevel"  %% "cats-effect" % CatsEffect
  , "com.chuusai"    %% "shapeless"   % Shapeless

    // Specialized
  , "co.fs2"       %% "fs2-core"            % FS2
  , "co.fs2"       %% "fs2-io"              % FS2
  , "org.tpolecat" %% "doobie-core"         % Doobie
  , "org.tpolecat" %% "doobie-postgres"     % Doobie
  , "io.circe"     %% "circe-core"          % Circe
  , "io.circe"     %% "circe-generic"       % Circe
  , "io.circe"     %% "circe-parser"        % Circe
  , "org.http4s"   %% "http4s-dsl"          % Http4s
  , "org.http4s"   %% "http4s-circe"        % Http4s
  , "org.http4s"   %% "http4s-blaze-server" % Http4s
  , "org.http4s"   %% "http4s-blaze-client" % Http4s
  , "com.softwaremill.sttp" %% "core"  % Sttp
  , "com.softwaremill.sttp" %% "circe" % Sttp

    // Test
  , "org.scalatest"  %% "scalatest"  % ScalaTest  % "test"
  , "org.scalacheck" %% "scalacheck" % ScalaCheck % "test"

    // Java Utility
  , "commons-io"              % "commons-io"    % CommonsIO
  , "org.seleniumhq.selenium" % "selenium-java" % Selenium
  , "org.jsoup"               % "jsoup"         % Jsoup
  , "org.slf4j"               % "slf4j-simple"  % SLF4J
  , "org.postgresql"          % "postgresql"    % PostgresDriver
  )

, addCompilerPlugin("org.spire-math" %% "kind-projector" % KindProjector)
, scalacOptions ++= Seq(
      "-deprecation"
    , "-encoding", "UTF-8"
    , "-feature"
    , "-language:existentials"
    , "-language:higherKinds"
    , "-language:implicitConversions"
    , "-language:experimental.macros"
    , "-unchecked"
    // , "-Xfatal-warnings"
    // , "-Xlint"
    // , "-Yinline-warnings"
    , "-Ywarn-dead-code"
    , "-Xfuture"
    , "-Ypartial-unification")
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    initialCommands := "import radar._, Main._"
  )
