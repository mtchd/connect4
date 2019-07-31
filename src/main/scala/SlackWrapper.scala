import SlackWrapper.messageUser
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object SlackWrapper {

  private val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("slack"/* config.getConfig("akka")*/)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in.
    */
  // TODO: Divide the command parsing and the side effects up
  def startListening(apiKeyPath: String): Unit = {

    val token = config.getString(apiKeyPath)

    val rtmClient = SlackRtmClient(token, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)

    var gameInstances: List[GameInstance] = List.empty

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      val (newGameInstances, reply) = CommandHandler.interpret(message.text, message.user, gameInstances)
      gameInstances = newGameInstances
      message.thread_ts match {
        case Some(thread_ts) => messageUser(reply, message.channel, Some(thread_ts), message.user, rtmClient)
        case None => messageUser(reply, message.channel, Some(message.ts), message.user, rtmClient)
      }

    }

  }

  def messageUser(message: Option[String], channel: String, thread_ts: Option[String], slackId: String, client: SlackRtmClient): Unit = {
    message match {
      case Some(messageText) => client.sendMessage(channel, s"<@$slackId>: $messageText", thread_ts)
      case None => ()
    }

  }
}
