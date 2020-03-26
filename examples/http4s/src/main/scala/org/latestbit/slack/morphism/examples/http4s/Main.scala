package org.latestbit.slack.morphism.examples.http4s

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.latestbit.slack.morphism.examples.http4s.config._

object Main extends IOApp with StrictLogging {

  val APP_NAME = "Slack-Morphism-Example"
  val APP_VER = "0.1.0"

  private def createParser() = {
    new scopt.OptionParser[AppConfig]( APP_NAME ) {
      head( APP_NAME, APP_VER )
      opt[String]( "host" ).abbr( "h" ).text( "HTTP Server Host" ).action { ( v, c ) => c.copy( httpServerHost = v ) }
      opt[Int]( "port" ).abbr( "p" ).text( "HTTP Server Port" ).action { ( v, c ) => c.copy( httpServerPort = v ) }
      opt[String]( "slack-app-id" ).abbr( "aid" ).text( "Slack App Id" ).required().action { ( v, c ) =>
        c.copy( slackAppConfig = c.slackAppConfig.copy( appId = v ) )
      }
      opt[String]( "slack-client-id" ).abbr( "cid" ).text( "Slack Client Id" ).required().action { ( v, c ) =>
        c.copy( slackAppConfig = c.slackAppConfig.copy( clientId = v ) )
      }
      opt[String]( "slack-client-secret" ).abbr( "cs" ).text( "Slack Client Secret" ).required().action { ( v, c ) =>
        c.copy( slackAppConfig = c.slackAppConfig.copy( clientSecret = v ) )
      }
      opt[String]( "slack-signing-secret" ).abbr( "ss" ).text( "Slack Signing Secret" ).required().action { ( v, c ) =>
        c.copy( slackAppConfig = c.slackAppConfig.copy( signingSecret = v ) )
      }
      opt[String]( "slack-redirect-url" ).abbr( "rurl" ).text( "Slack Redirect URL" ).action { ( v, c ) =>
        c.copy( slackAppConfig = c.slackAppConfig.copy( redirectUrl = Some( v ) ) )
      }
      opt[String]( "slack-install-bot-scope" )
        .abbr( "bot-scope" )
        .text( "Slack OAuth Install Scope for a bot token" )
        .action { ( v, c ) => c.copy( slackAppConfig = c.slackAppConfig.copy( botScope = v ) ) }
      opt[String]( "sway-db-path" ).abbr( "dbpath" ).text( "Path to data for SwayDb" ).action { ( v, c ) =>
        c.copy( databaseDir = v )
      }
    }
  }

  def run( args: List[String] ) = {
    val parser = createParser()
    parser.parse( args, AppConfig( slackAppConfig = SlackAppConfig.empty ) ) match {
      case Some( config: AppConfig ) => {
        logger.info( "Loading..." )
        Http4sServer.stream[IO]( config ).compile.drain.as( ExitCode.Success )
      }

      case _ => {
        parser.showTryHelp()
        IO.pure( ExitCode.Error )
      }
    }
  }
}
