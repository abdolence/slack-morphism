package org.latestbit.slack.morphism.examples.http4s

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging

import com.monovore.decline._
import com.monovore.decline.effect._

import org.latestbit.slack.morphism.examples.http4s.config._

object Main
    extends CommandIOApp( name = Main.APP_NAME, version = Main.APP_VER, header = Main.APP_DESC_HEADER )
    with StrictLogging {

  final val APP_NAME = "Slack-Morphism-Example"
  final val APP_VER = "0.1.0"
  final val APP_DESC_HEADER = "Slack Morphism Example Bot For http4s"

  private val parseSlackConfigOpts: Opts[SlackAppConfig] = {
    (
      Opts.option[String]( long = "slack-app-id", short = "aid", help = "Slack App Id" ),
      Opts.option[String]( long = "slack-client-id", short = "cid", help = "Slack Client Id" ),
      Opts.option[String]( long = "slack-client-secret", short = "cs", help = "Slack Client Secret" ),
      Opts.option[String]( long = "slack-signing-secret", short = "ss", help = "Slack Signing Secret" ),
      Opts
        .option[String]( long = "slack-redirect-url", short = "rurl", help = "Slack OAuth Redirect URL" )
        .orNone,
      Opts
        .option[String](
          long = "slack-install-bot-scope",
          short = "bot-scope",
          help = "Slack OAuth Install Scope for a bot token"
        )
        .withDefault( SlackAppConfig.defaultScope )
    ).mapN( SlackAppConfig.apply )
  }

  private val parseAppConfigOpts: Opts[AppConfig] = {
    (
      Opts.option[String]( long = "host", short = "h", help = "HTTP Server Host" ).withDefault( AppConfig.defaultHost ),
      Opts.option[Int]( long = "port", short = "p", help = "HTTP Server Port" ).withDefault( AppConfig.defaultPort ),
      parseSlackConfigOpts,
      Opts
        .option[String]( long = "sway-db-path", short = "dbpath", help = "Path to data for SwayDb" )
        .withDefault( AppConfig.defaultDatabaseDir )
    ).mapN( AppConfig.apply )
  }

  override def main: Opts[IO[ExitCode]] = {
    parseAppConfigOpts.map { config: AppConfig =>
      logger.info( "Loading..." )
      Http4sServer.stream[IO]( config ).compile.drain.as( ExitCode.Success )
    }
  }
}
