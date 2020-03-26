import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }

import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.DefaultReadableGit
import microsites._
import sbt.Package.ManifestAttributes

name := "slack-morphism-root"

ThisBuild / version := "1.2.4"

ThisBuild / description := "Open Type-Safe Reactive Client with Blocks Templating for Slack"

ThisBuild / organization := "org.latestbit"

ThisBuild / homepage := Some( url( "https://slack.abdolence.dev" ) )

ThisBuild / licenses := Seq(
  ( "Apache License v2.0", url( "http://www.apache.org/licenses/LICENSE-2.0.html" ) )
)

ThisBuild / crossScalaVersions := Seq( "2.13.1", "2.12.11" )

ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / sbtVersion := "1.3.8"

ThisBuild / scalacOptions ++= Seq( "-feature" )

ThisBuild / exportJars := true

ThisBuild / exportJars := true

ThisBuild / publishMavenStyle := true

ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some( "snapshots" at nexus + "content/repositories/snapshots" )
  else
    Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
}

ThisBuild / pomExtra := (
  <scm>
    <url>https://github.com/abdolence/slack-morphism</url>
    <connection>scm:git:https://github.com/abdolence/slack-morphism</connection>
    <developerConnection>scm:git:https://github.com/abdolence/slack-morphism</developerConnection>
  </scm>
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

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
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

def priorTo2_13( scalaVersion: String ): Boolean =
  CrossVersion.partialVersion( scalaVersion ) match {
    case Some( ( 2, minor ) ) if minor < 13 => true
    case _                                  => false
  }

val catsVersion = "2.1.1"
val catsEffectVersion = "2.1.2"
val circeVersion = "0.13.0"
val scalaCollectionsCompatVersion = "2.1.3"
val sttpVersion = "2.0.6"
val circeAdtCodecVersion = "0.9.0"
val reactiveStreamsVersion = "1.0.3"

// For tests
val scalaTestVersion = "3.1.0"
val scalaCheckVersion = "1.14.3"
val scalaTestPlusCheck = "3.1.1.1"
val scalaTestPlusTestNG = "3.1.0.0" // reactive publishers tck testing
val scalaCheckShapeless = "1.2.3"
val scalaMockVersion = "4.4.0"

// For full-featured examples we use additional libs
val akkaVersion = "2.5.27"
val akkaHttpVersion = "10.1.11"
val akkaHttpCirceVersion = "1.30.0"
val logbackVersion = "1.2.3"
val scalaLoggingVersion = "3.9.2"
val scoptVersion = "3.7.1"
val swayDbVersion = "0.11"
val http4sVersion = "0.21.1"

// Compiler plugins
val kindProjectorVer = "0.11.0"

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
      "io.circe" %% "circe-parser"
    ).map(
      _ % circeVersion
        exclude ("org.typelevel", "cats-core")
    ) ++
    Seq(
      "org.latestbit" %% "circe-tagged-adt-codec" % circeAdtCodecVersion
        excludeAll (ExclusionRule( organization = "io.circe" ) )
    ) ++
    Seq(
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion,
      "org.scalacheck" %% "scalacheck" % scalaCheckVersion,
      "org.scalamock" %% "scalamock" % scalaMockVersion,
      "org.typelevel" %% "cats-laws" % catsVersion,
      "org.typelevel" %% "cats-testkit" % catsVersion,
      "org.reactivestreams" % "reactive-streams-tck" % reactiveStreamsVersion,
      "org.scalatestplus" %% "scalacheck-1-14" % scalaTestPlusCheck,
      "org.scalatestplus" %% "testng-6-7" % scalaTestPlusTestNG,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapeless,
      "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % sttpVersion,
      "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % sttpVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion
        exclude ("org.slf4j", "slf4j-api"),
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
    ).map(
      _ % "test"
        exclude ("org.typelevel", "cats-core")
        exclude ("org.typelevel", "cats-effect")
    )

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val scalaDocSettings = Seq(
  scalacOptions in (Compile, doc) ++= Seq( "-groups", "-skip-packages", "sttp.client" ) ++
    (if (priorTo2_13( scalaVersion.value ))
       Seq( "-Yno-adapted-args" )
     else
       Seq( "-Ymacro-annotations" ))
)

lazy val compilerPluginSettings = Seq(
  addCompilerPlugin( "org.typelevel" % "kind-projector" % kindProjectorVer cross CrossVersion.full )
)

lazy val slackMorphismRoot = project
  .in( file( "." ) )
  .aggregate( slackMorphismModels, slackMorphismClient, slackMorphismAkkaExample, slackMorphismHttp4sExample )
  .settings( noPublishSettings )

lazy val slackMorphismModels =
  (project in file( "models" ))
    .settings(
      name := "slack-morphism-models",
      libraryDependencies ++= baseDependencies ++ Seq()
    )
    .settings( scalaDocSettings )
    .settings( compilerPluginSettings )

lazy val slackMorphismClient =
  (project in file( "client" ))
    .settings(
      name := "slack-morphism-client",
      libraryDependencies ++= (baseDependencies ++ Seq(
        "com.softwaremill.sttp.client" %% "core" % sttpVersion,
        "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollectionsCompatVersion,
        "org.reactivestreams" % "reactive-streams" % reactiveStreamsVersion
      ) ++ (if (priorTo2_13( scalaVersion.value ))
              Seq( "com.github.bigwheel" %% "util-backports" % bigwheelUtilBackports )
            else Seq()))
    )
    .settings( scalaDocSettings )
    .settings( compilerPluginSettings )
    .dependsOn( slackMorphismModels )

lazy val slackMorphismAkkaExample =
  (project in file( "examples/akka-http" ))
    .settings(
      name := "slack-morphism-akka",
      libraryDependencies ++= baseDependencies ++ Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion
          excludeAll (
            ExclusionRule( organization = "org.reactivestreams" )
          ),
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.github.scopt" %% "scopt" % scoptVersion,
        "ch.qos.logback" % "logback-classic" % logbackVersion
          exclude ("org.slf4j", "slf4j-api"),
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
        "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion
          excludeAll (
            ExclusionRule( organization = "com.typesafe.akka" ),
            ExclusionRule( organization = "io.circe" )
        ),
        "com.softwaremill.sttp.client" %% "akka-http-backend" % sttpVersion,
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
  (project in file( "examples/http4s" ))
    .settings(
      name := "slack-morphism-http4s",
      libraryDependencies ++= baseDependencies ++ Seq(
        "org.http4s" %% "http4s-blaze-server" % http4sVersion,
        "org.http4s" %% "http4s-blaze-client" % http4sVersion,
        "org.http4s" %% "http4s-circe" % http4sVersion,
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "com.github.scopt" %% "scopt" % scoptVersion,
        "ch.qos.logback" % "logback-classic" % logbackVersion
          exclude ("org.slf4j", "slf4j-api"),
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
        "com.softwaremill.sttp.client" %% "http4s-backend" % sttpVersion,
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

lazy val apiDocsDir = settingKey[String]( "Name of subdirectory for api docs" )

lazy val docSettings = Seq(
  micrositeName := "Slack Morphism for Scala",
  micrositeUrl := "https://slack.abdolence.dev",
  micrositeDocumentationUrl := "/docs",
  micrositeDocumentationLabelDescription := "Docs",
  micrositeAuthor := "Abdulla Abdurakhmanov",
  micrositeHomepage := "https://slack.abdolence.dev",
  micrositeOrganizationHomepage := "https://abdolence.dev",
  micrositeGithubOwner := "abdolence",
  micrositeGithubRepo := "slack-morphism",
  micrositePushSiteWith := GHPagesPlugin,
  autoAPIMappings := true,
  micrositeTheme := "light",
  micrositePalette := Map(
    "brand-primary" -> "#bf360c",
    "brand-secondary" -> "#37474f",
    "white-color" -> "#FFFFFF"
  ),
  micrositeGithubToken := sys.env.get( "GITHUB_TOKEN" ),
  micrositeGitterChannel := false,
  micrositeFooterText := None,
  micrositeFavicons := Seq(
    MicrositeFavicon( "favicon-16x16.png", "16x16" ),
    MicrositeFavicon( "favicon-32x32.png", "32x32" ),
    MicrositeFavicon( "favicon-96x96.png", "96x96" ),
    MicrositeFavicon( "favicon-128.png", "128x128" ),
    MicrositeFavicon( "favicon-196x196.png", "196x196" )
  ),
  apiDocsDir := "api",
  unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects( slackMorphismModels, slackMorphismClient ),
  addMappingsToSiteDir( mappings in (ScalaUnidoc, packageDoc), apiDocsDir ),
  micrositeAnalyticsToken := "UA-155371094-1",
  includeFilter in makeSite := (includeFilter in makeSite).value || "*.txt" || "*.xml",
  mappings in makeSite ++= Seq(
    (resourceDirectory in Compile).value / "microsite" / "robots.txt" -> "robots.txt",
    (resourceDirectory in Compile).value / "microsite" / "sitemap.xml" -> "sitemap.xml"
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
