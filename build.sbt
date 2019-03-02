name := """fs2-nakadi"""

val http4sVersion = "0.19.0"
val circeVersion  = "0.10.1"

scalaVersion in ThisBuild := "2.12.8"

organization := "org.zalando"

fork in Test := true
parallelExecution in Test := true
testForkedParallel in Test := true

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",  // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
  //  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xfuture", // Turn on future language features.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
  "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
  "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
  "-Xlint:option-implicit", // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",         // Pattern match may not be typesafe.
  //"-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification", // Enable partial unification in type constructor inference
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")
addCompilerPlugin(("org.scalameta" % "paradise" % "3.0.0-M11").cross(CrossVersion.full))

libraryDependencies ++= Seq(
  "org.typelevel"              %% "cats-free"           % "1.6.0",
  "org.typelevel"              %% "cats-tagless-macros" % "0.1.0",
  "io.circe"                   %% "circe-core"          % circeVersion,
  "io.circe"                   %% "circe-java8"         % circeVersion,
  "io.circe"                   %% "circe-parser"        % circeVersion,
  "io.circe"                   %% "circe-derivation"    % "0.10.0-M1",
  "org.http4s"                 %% "http4s-dsl"          % http4sVersion,
  "org.http4s"                 %% "http4s-blaze-client" % http4sVersion,
  "org.http4s"                 %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"                 %% "http4s-core"         % http4sVersion,
  "org.http4s"                 %% "http4s-circe"        % http4sVersion,
  "com.beachape"               %% "enumeratum-circe"    % "1.5.20",
  "com.typesafe.scala-logging" %% "scala-logging"       % "3.8.0",
  "ch.qos.logback"             % "logback-classic"      % "1.1.7",
  "org.specs2"                 %% "specs2-core"         % "3.8.9" % Test,
  "org.scalatest"              %% "scalatest"           % "3.0.5" % Test
)
