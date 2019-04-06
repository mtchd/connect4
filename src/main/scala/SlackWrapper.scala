import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in.
    */
  // TODO: Divide the command parsing and the side effects up
  def startListening(apiKeyPath: String): Unit = {

    val client = SlackRtmClient(ConfigFactory.load().getString(apiKeyPath))
    val selfId: String = client.state.self.id

    var gameInstances: List[GameInstance] = List.empty

    println("starting to listen...")

    client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      // TODO: Add threading functionality
      // Out of game commands
      if (mentionedIds.contains(selfId)) {

        // Clean the @connect4 off the message, as the number can cause a false positive with Drop?
        // TODO: Better way of doing this
        val cleanedText = message.text match {
          case CommandsRegex.Clean(_, cleanedMessage) => cleanedMessage
          case _ => message.text
        }

        val (newGameInstances, reply) = CommandHandler.interpret(cleanedText, message.user, gameInstances)
        gameInstances = newGameInstances
        println(message.thread_ts)
        client.sendMessage(message.channel, s"<@${message.user}>: $reply", message.thread_ts)
      }
    }
  }

//  def messageUser(message: String, channel: String, thread_ts: Option[String], slackId: String): Unit = {
//    client.sendMessage(channel, s"<@$slackId>: $message", thread_ts)
//  }
}
