import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }

import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.DefaultReadableGit
import microsites._
import sbt.Package.ManifestAttributes

name := "slack-morphism-root"

ThisBuild / version := "4.0.1"

ThisBuild / versionScheme := Some( "semver-spec" )

ThisBuild / description := "Open Type-Safe Reactive Client with Blocks Templating for Slack"

ThisBuild / organization := "org.latestbit"

ThisBuild / homepage := Some( url( "https://slack.abdolence.dev" ) )

ThisBuild / licenses := Seq(
  ( "Apache License v2.0", url( "http://www.apache.org/licenses/LICENSE-2.0.html" ) )
)

ThisBuild / crossScalaVersions := Seq( "2.13.7", "2.12.15" )

ThisBuild / scalaVersion := ( ThisBuild / crossScalaVersions ).value.head

ThisBuild / scalacOptions ++= Seq( "-feature" )

ThisBuild / exportJars := true

ThisBuild / exportJars := true

ThisBuild / semanticdbEnabled := true

ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion( scalaVersion.value )

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some( "snapshots" at nexus + "content/repositories/snapshots" )
  else
    Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
}

ThisBuild / pomExtra := (
  <developers>
    <developer>
        <id>abdolence</id>
        <name>Abdulla Abdurakhmanov</name>
        <url>https://abdolence.dev</url>
    </developer>
  </developers>
)

ThisBuild / resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

ThisBuild / scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard" /*,
  "-Ywarn-unused"*/
) ++ ( CrossVersion.partialVersion( scalaVersion.value ) match {
  case Some( ( 2, n ) ) if n >= 13 => Seq( "-Xsource:3" )
  case Some( ( 2, n ) ) if n < 13  => Seq( "-Ypartial-unification" )
  case _                           => Seq()
} )

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

def priorTo2_13( scalaVersion: String ): Boolean =
  CrossVersion.partialVersion( scalaVersion ) match {
    case Some( ( 2, minor ) ) if minor < 13 => true
    case _                                  => false
  }

val catsVersion                   = "2.7.0"
val catsEffectVersion             = "3.3.6"
val circeVersion                  = "0.14.1"
val scalaCollectionsCompatVersion = "2.6.0"
val sttp3Version                  = "3.4.2"
val circeAdtCodecVersion          = "0.10.0"

// For tests
val scalaTestVersion    = "3.2.11"
val scalaCheckVersion   = "1.15.4"
val scalaTestPlusCheck  = "3.2.2.0"
val scalaTestPlusTestNG = "3.2.10.0" // reactive publishers tck testing
val scalaCheckShapeless = "1.2.5"
val scalaMockVersion    = "5.2.0"

// For full-featured examples we use additional libs
val akkaVersion          = "2.6.18"
val akkaHttpVersion      = "10.2.7"
val akkaHttpCirceVersion = "1.39.2"
val logbackVersion       = "1.2.10"
val scalaLoggingVersion  = "3.9.4"
val scoptVersion         = "3.7.1"
val swayDbVersion        = "0.16.2"
val http4sVersion        = "0.23.10"
val declineVersion       = "2.2.0"

// For fs2 integration module
val fs2Version = "3.2.5"

// For reactive-streams integration module
val reactiveStreamsVersion = "1.0.3"

// Compiler plugins
val kindProjectorVer = "0.13.2"

// Compatibility libs for Scala < 2.13
val bigwheelUtilBackports = "2.1"

val baseDependencies =
  Seq(
    "org.typelevel" %% "cats-core"
  ).map( _ % catsVersion ) ++
    Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion
    ) ++
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-generic-extras",
      "io.circe" %% "circe-parser"
    ).map(
      _ % circeVersion
        exclude ( "org.typelevel", "cats-core" )
    ) ++
    Seq(
      "org.latestbit" %% "circe-tagged-adt-codec" % circeAdtCodecVersion
        excludeAll ( ExclusionRule( organization = "io.circe" ) )
    ) ++
    Seq(
      "org.scalactic"                 %% "scalactic"                        % scalaTestVersion,
      "org.scalatest"                 %% "scalatest"                        % scalaTestVersion,
      "org.scalacheck"                %% "scalacheck"                       % scalaCheckVersion,
      "org.scalamock"                 %% "scalamock"                        % scalaMockVersion,
      "org.typelevel"                 %% "cats-laws"                        % catsVersion,
      "org.typelevel"                 %% "cats-testkit"                     % catsVersion,
      "org.scalatestplus"             %% "scalacheck-1-14"                  % scalaTestPlusCheck,
      "org.scalatestplus"             %% "testng-6-7"                       % scalaTestPlusTestNG,
      "com.github.alexarchambault"    %% "scalacheck-shapeless_1.14"        % scalaCheckShapeless,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-future" % sttp3Version,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats"   % sttp3Version,
      // "com.softwaremill.sttp.client3" %% "async-http-client-backend-monix"  % sttp3Version,
      "com.softwaremill.sttp.client3" %% "http4s-backend"      % sttp3Version,
      "org.http4s"                    %% "http4s-blaze-client" % http4sVersion
        exclude ( "org.typelevel", "cats-core" )
        exclude ( "org.typelevel", "cats-effect" )
        excludeAll ( ExclusionRule( organization = "io.circe" ) ),
      "ch.qos.logback" % "logback-classic" % logbackVersion
        exclude ( "org.slf4j", "slf4j-api" ),
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
    ).map(
      _ % Test
        exclude ( "org.typelevel", "cats-core" )
        exclude ( "org.typelevel", "cats-effect" )
    )

lazy val noPublishSettings = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false
)

lazy val overwritePublishSettings = Seq(
  publishConfiguration := publishConfiguration.value.withOverwrite( true )
)

lazy val scalaDocSettings = Seq(
  Compile / doc / scalacOptions ++= Seq( "-groups", "-skip-packages", "sttp.client" ) ++
    ( if (priorTo2_13( scalaVersion.value ))
        Seq( "-Yno-adapted-args" )
      else
        Seq( "-Ymacro-annotations" ) )
)

lazy val compilerPluginSettings = Seq(
  addCompilerPlugin( "org.typelevel" % "kind-projector" % kindProjectorVer cross CrossVersion.full )
)

lazy val slackMorphismRoot = project
  .in( file( "." ) )
  .aggregate(
    slackMorphismModels,
    slackMorphismClient,
    slackMorphismAkkaExample,
    slackMorphismHttp4sExample,
    slackMorphismFs2,
    slackMorphismReactiveStreams
  )
  .settings( noPublishSettings )

lazy val slackMorphismModels =
  ( project in file( "models" ) )
    .settings(
      name := "slack-morphism-models",
      libraryDependencies ++= baseDependencies ++ Seq()
    )
    .settings( scalaDocSettings )
    .settings( compilerPluginSettings )
    .settings( overwritePublishSettings )

lazy val slackMorphismClient =
  ( project in file( "client" ) )
    .settings(
      name := "slack-morphism-client",
      libraryDependencies ++= ( baseDependencies ++ Seq(
        "com.softwaremill.sttp.client3" %% "core"                    % sttp3Version,
        "org.scala-lang.modules"        %% "scala-collection-compat" % scalaCollectionsCompatVersion
      ) ++ ( if (priorTo2_13( scalaVersion.value ))
               Seq( "com.github.bigwheel" %% "util-backports" % bigwheelUtilBackports )
             else Seq() ) )
    )
    .settings( scalaDocSettings )
    .settings( compilerPluginSettings )
    .settings( overwritePublishSettings )
    .dependsOn( slackMorphismModels )

lazy val slackMorphismAkkaExample =
  ( project in file( "examples/akka-http" ) )
    .settings(
      name := "slack-morphism-akka",
      libraryDependencies ++= baseDependencies ++ Seq(
        "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-actor-typed"  % akkaVersion,
        "com.github.scopt"  %% "scopt"             % scoptVersion,
        "ch.qos.logback"     % "logback-classic"   % logbackVersion
          exclude ( "org.slf4j", "slf4j-api" ),
        "com.typesafe.scala-logging" %% "scala-logging"   % scalaLoggingVersion,
        "de.heikoseeberger"          %% "akka-http-circe" % akkaHttpCirceVersion
          excludeAll (
            ExclusionRule( organization = "com.typesafe.akka" ),
            ExclusionRule( organization = "io.circe" )
          ),
        "com.softwaremill.sttp.client3" %% "akka-http-backend" % sttp3Version
          excludeAll (
            ExclusionRule( organization = "com.typesafe.akka" )
          ),
        "io.swaydb" %% "swaydb" % swayDbVersion
          excludeAll (
            ExclusionRule( organization = "org.scala-lang.modules" ),
            ExclusionRule( organization = "org.reactivestreams" )
          )
      )
    )
    .settings( noPublishSettings )
    .settings( compilerPluginSettings )
    .dependsOn( slackMorphismClient )

lazy val slackMorphismHttp4sExample =
  ( project in file( "examples/http4s" ) )
    .settings(
      name := "slack-morphism-http4s",
      libraryDependencies ++= baseDependencies ++ ( Seq(
        "org.http4s" %% "http4s-blaze-server",
        "org.http4s" %% "http4s-blaze-client",
        "org.http4s" %% "http4s-circe",
        "org.http4s" %% "http4s-dsl"
      ).map(
        _ % http4sVersion
          exclude ( "org.typelevel", "cats-core" )
          exclude ( "org.typelevel", "cats-effect" )
          excludeAll ( ExclusionRule( organization = "io.circe" ) )
      ) ) ++ Seq(
        "com.monovore" %% "decline" % declineVersion
          exclude ( "org.typelevel", "cats-core" ),
        "com.monovore" %% "decline-effect" % declineVersion
          exclude ( "org.typelevel", "cats-core" )
          exclude ( "org.typelevel", "cats-effect" ),
        "ch.qos.logback" % "logback-classic" % logbackVersion
          exclude ( "org.slf4j", "slf4j-api" ),
        "com.typesafe.scala-logging"    %% "scala-logging"  % scalaLoggingVersion,
        "com.softwaremill.sttp.client3" %% "http4s-backend" % sttp3Version
          excludeAll ( ExclusionRule( organization = "org.http4s" ) )
          excludeAll ( ExclusionRule( organization = "io.circe" ) ),
        "io.swaydb" %% "swaydb" % swayDbVersion
          excludeAll (
            ExclusionRule( organization = "org.scala-lang.modules" ),
            ExclusionRule( organization = "org.reactivestreams" )
          ),
        "io.swaydb" %% "cats-effect" % swayDbVersion
          excludeAll (
            ExclusionRule( organization = "org.scala-lang.modules" ),
            ExclusionRule( organization = "org.reactivestreams" ),
            ExclusionRule( organization = "org.typelevel" )
          )
      )
    )
    .settings( noPublishSettings )
    .settings( compilerPluginSettings )
    .dependsOn( slackMorphismClient )

lazy val slackMorphismFs2 =
  ( project in file( "fs2" ) )
    .settings(
      name := "slack-morphism-fs2",
      libraryDependencies ++= baseDependencies ++ Seq(
        "co.fs2" %% "fs2-core" % fs2Version
          exclude ( "org.typelevel", "cats-core" )
          exclude ( "org.typelevel", "cats-effect" )
          excludeAll ( ExclusionRule( organization = "io.circe" ) )
      )
    )
    .settings( scalaDocSettings )
    .settings( compilerPluginSettings )
    .settings( overwritePublishSettings )
    .dependsOn( slackMorphismClient % "compile->compile;test->test" )

lazy val slackMorphismReactiveStreams =
  ( project in file( "reactive-streams" ) )
    .settings(
      name := "slack-morphism-reactive-streams",
      libraryDependencies ++= baseDependencies ++ Seq(
        "org.reactivestreams" % "reactive-streams" % reactiveStreamsVersion
      ) ++ ( Seq(
        "org.reactivestreams" % "reactive-streams-tck" % reactiveStreamsVersion
      ).map( _ % Test ) )
    )
    .settings( scalaDocSettings )
    .settings( compilerPluginSettings )
    .settings( overwritePublishSettings )
    .dependsOn( slackMorphismClient % "compile->compile;test->test" )

lazy val apiDocsDir = settingKey[String]( "Name of subdirectory for api docs" )

lazy val docSettings = Seq(
  micrositeName                          := "Slack Morphism for Scala",
  micrositeUrl                           := "https://slack.abdolence.dev",
  micrositeDocumentationUrl              := "/docs",
  micrositeDocumentationLabelDescription := "Docs",
  micrositeAuthor                        := "Abdulla Abdurakhmanov",
  micrositeHomepage                      := "https://slack.abdolence.dev",
  micrositeOrganizationHomepage          := "https://abdolence.dev",
  micrositeGithubOwner                   := "abdolence",
  micrositeGithubRepo                    := "slack-morphism",
  micrositePushSiteWith                  := GHPagesPlugin,
  autoAPIMappings                        := true,
  micrositeTheme                         := "light",
  micrositePalette := Map(
    "brand-primary"   -> "#bf360c",
    "brand-secondary" -> "#37474f",
    "white-color"     -> "#FFFFFF"
  ),
  micrositeGithubToken   := sys.env.get( "GITHUB_TOKEN" ),
  micrositeGitterChannel := false,
  micrositeFooterText    := None,
  micrositeFavicons := Seq(
    MicrositeFavicon( "favicon-16x16.png", "16x16" ),
    MicrositeFavicon( "favicon-32x32.png", "32x32" ),
    MicrositeFavicon( "favicon-96x96.png", "96x96" ),
    MicrositeFavicon( "favicon-128.png", "128x128" ),
    MicrositeFavicon( "favicon-196x196.png", "196x196" )
  ),
  apiDocsDir                                 := "api",
  ScalaUnidoc / unidoc / unidocProjectFilter := inProjects( slackMorphismModels, slackMorphismClient ),
  addMappingsToSiteDir( ScalaUnidoc / packageDoc / mappings, apiDocsDir ),
  makeSite / includeFilter := ( makeSite / includeFilter ).value || "*.txt" || "*.xml",
  makeSite / mappings ++= Seq(
    ( Compile / resourceDirectory ).value / "microsite" / "robots.txt"  -> "robots.txt",
    ( Compile / resourceDirectory ).value / "microsite" / "sitemap.xml" -> "sitemap.xml"
  )
)

ThisBuild / GitKeys.gitReader := baseDirectory( base => new DefaultReadableGit( base ) ).value

addCompilerPlugin( "org.typelevel" % "kind-projector" % kindProjectorVer cross CrossVersion.full )

lazy val slackMorphismMicrosite = project
  .in( file( "site" ) )
  .settings(
    name := "slack-morphism-microsite"
  )
  .settings( noPublishSettings )
  .settings( compilerPluginSettings )
  .settings( docSettings )
  .settings( scalaDocSettings )
  .enablePlugins( MicrositesPlugin )
  .enablePlugins( ScalaUnidocPlugin )
  .dependsOn( slackMorphismModels, slackMorphismClient, slackMorphismAkkaExample )

addCommandAlias( "publishAllDocs", ";slackMorphismMicrosite/publishMicrosite" )
