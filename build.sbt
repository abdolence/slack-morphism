import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }

import sbt.Package.ManifestAttributes

name := "slack-morphism-root"

ThisBuild / version := "1.0.0-SNAPSHOT"

ThisBuild / organization := "org.latestbit"

ThisBuild / homepage := Some( url( "https://latestbit.com" ) )

ThisBuild / licenses := Seq(
  ( "Apache License v2.0", url( "http://www.apache.org/licenses/LICENSE-2.0.html" ) )
)

ThisBuild / crossScalaVersions := Seq( "2.13.1", "2.12.10" )

ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / sbtVersion := "1.3.5"

ThisBuild / scalacOptions ++= Seq( "-feature" )

ThisBuild / exportJars := true

publishArtifact := false

publishTo := Some( Resolver.file( "Unused transient repository", file( "target/unusedrepo" ) ) )

ThisBuild / resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:higherKinds"
) ++ (CrossVersion.partialVersion( (ThisBuild / scalaVersion).value ) match {
  case Some( ( 2, n ) ) if n >= 13 => Seq( "-Xsource:2.14" )
  case Some( ( 2, n ) ) if n < 13  => Seq( "-Ypartial-unification" )
  case _                           => Seq()
})

ThisBuild / javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-source",
  "1.8",
  "-target",
  "1.8",
  "-Xlint"
)

ThisBuild / packageOptions := Seq(
  ManifestAttributes(
    ( "Build-Jdk", System.getProperty( "java.version" ) ),
    (
      "Build-Date",
      ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_OFFSET_DATE_TIME )
    )
  )
)

val catsVersion = "2.0.0"
val circeVersion = "0.12.3"
val scalaTestVersion = "3.1.0"
val scalaCollectionsCompatVersion = "2.1.3"
val scalaCheckVersion = "1.14.3"
val sttpVersion = "2.0.0-RC5"
val circeAdtCodecVersion = "0.4.1"
val reactiveStreamsVersion = "1.0.3"

val baseDependencies =
  Seq(
    "org.typelevel" %% "cats-core"
  ).map( _ % catsVersion ) ++
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map( _ % circeVersion ) ++
    Seq(
      "org.latestbit" %% "circe-tagged-adt-codec" % circeAdtCodecVersion
    ) ++
    Seq(
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion,
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
      "org.typelevel" %% "cats-laws" % catsVersion,
      "org.typelevel" %% "cats-testkit" % catsVersion,
      "org.reactivestreams" % "reactive-streams-tck" % reactiveStreamsVersion,
      "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
      "org.scalatestplus" %% "testng-6-7" % "3.1.0.0",
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.3"
    ).map( _ % "test" )

//addCompilerPlugin( "org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.full )

lazy val slackMorphismRoot = project
  .in( file( "." ) )
  .aggregate( slackMorphismModels, slackMorphismClient, slackMorphismExamples )
  .settings(
    publish := {},
    publishLocal := {},
    crossScalaVersions := List()
  )

lazy val slackMorphismModels =
  (project in file( "models" )).settings(
    name := "slack-morphism-models",
    libraryDependencies ++= baseDependencies ++ Seq()
  )

lazy val slackMorphismClient =
  (project in file( "client" ))
    .settings(
      name := "slack-morphism-client",
      libraryDependencies ++= baseDependencies ++ Seq(
        "com.softwaremill.sttp.client" %% "core" % sttpVersion,
        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionsCompatVersion,
        "org.reactivestreams" % "reactive-streams" % reactiveStreamsVersion
      )
    )
    .dependsOn( slackMorphismModels )

lazy val slackMorphismExamples =
  (project in file( "examples" ))
    .settings(
      name := "slack-morphism-client",
      libraryDependencies ++= baseDependencies ++ Seq()
    )
    .dependsOn( slackMorphismClient )
