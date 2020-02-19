import java.time.format.DateTimeFormatter
import java.time.{ ZoneOffset, ZonedDateTime }

import com.typesafe.sbt.SbtGit.GitKeys
import com.typesafe.sbt.git.DefaultReadableGit
import microsites._
import sbt.Package.ManifestAttributes

name := "slack-morphism-root"

ThisBuild / version := "1.0.1-SNAPSHOT"

ThisBuild / description := "Open Type-Safe Reactive Client with Blocks Templating for Slack"

ThisBuild / organization := "org.latestbit"

ThisBuild / homepage := Some( url( "https://slack.abdolence.dev" ) )

ThisBuild / licenses := Seq(
  ( "Apache License v2.0", url( "http://www.apache.org/licenses/LICENSE-2.0.html" ) )
)

ThisBuild / crossScalaVersions := Seq( "2.13.1", "2.12.10" )

ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / sbtVersion := "1.3.5"

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

val catsVersion = "2.0.0"
val circeVersion = "0.13.0"
val scalaCollectionsCompatVersion = "2.1.3"
val sttpVersion = "2.0.0-RC9"
val circeAdtCodecVersion = "0.7.0"
val reactiveStreamsVersion = "1.0.3"

// For tests
val scalaTestVersion = "3.1.0"
val scalaCheckVersion = "1.14.3"
val scalaTestPlusCheck = "3.1.0.0-RC2"
val scalaTestPlusTestNG = "3.1.0.0" // for reactive publisher tck testing
val scalaCheckShapeless = "1.2.3"

// For full-featured examples we use additional libs like akka-http
val akkaVersion = "2.5.27"
val akkaHttpVersion = "10.1.11"
val akkaHttpCirceVersion = "1.30.0"
val logbackVersion = "1.2.3"
val scalaLoggingVersion = "3.9.2"
val scoptVersion = "3.7.1"
val swayDbVersion = "0.11"

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
      "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestPlusCheck,
      "org.scalatestplus" %% "testng-6-7" % scalaTestPlusTestNG,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapeless,
      "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % sttpVersion
    ).map( _ % "test" ) ++
    Seq(
      compilerPlugin( "org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full )
    )

//addCompilerPlugin( "org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.full )

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val scalaDocSettings = Seq(
  scalacOptions in (Compile, doc) := Seq( "-groups", "-skip-packages", "sttp.client" )
)

lazy val slackMorphismRoot = project
  .in( file( "." ) )
  .aggregate( slackMorphismModels, slackMorphismClient, slackMorphismExamples )
  .settings( noPublishSettings )

lazy val slackMorphismModels =
  (project in file( "models" ))
    .settings(
      name := "slack-morphism-models",
      libraryDependencies ++= baseDependencies ++ Seq()
    )
    .settings( scalaDocSettings )

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
    .settings( scalaDocSettings )
    .dependsOn( slackMorphismModels )

lazy val slackMorphismExamples =
  (project in file( "examples/akka-http" ))
    .settings(
      name := "slack-morphism-akka",
      libraryDependencies ++= baseDependencies ++ Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.github.scopt" %% "scopt" % scoptVersion,
        "ch.qos.logback" % "logback-classic" % logbackVersion,
        "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
        "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion
          excludeAll (ExclusionRule( organization = "com.typesafe.akka" ) ),
        "com.softwaremill.sttp.client" %% "akka-http-backend" % sttpVersion,
        "io.swaydb" %% "swaydb" % swayDbVersion
      )
    )
    .settings( noPublishSettings )
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
  micrositePushSiteWith := GitHub4s,
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
  ),
  libraryDependencies ++= baseDependencies
)

ThisBuild / GitKeys.gitReader := baseDirectory(base => new DefaultReadableGit( base ) ).value

lazy val slackMorphismMicrosite = project
  .in( file( "site" ) )
  .enablePlugins( MicrositesPlugin )
  .enablePlugins( ScalaUnidocPlugin )
  .settings(
    name := "slack-morphism-microsite"
  )
  .settings( noPublishSettings )
  .settings( docSettings )
  .settings( scalaDocSettings )
  .dependsOn( slackMorphismModels, slackMorphismClient, slackMorphismExamples )
