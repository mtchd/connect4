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

    println("Now listening to Slack...")

    client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      // First, check if the message is in a thread. If it is, then we check if that thread fits one of our game
      // instances.
      // Then, we need to play exclusively on that gameInstance.
      // If it doesn't have a game associated with it, check if we are mentioned in it.

      // Two options here. We can take that single game instances, put it in a list, and feed it to interpret. Or make custom code.

      // TODO: Add threading functionality
      // Out of game commands
      if (mentionedIds.contains(selfId)) {
        val (newGameInstances, reply) = CommandHandler.interpret(message.text, message.user, gameInstances)
        gameInstances = newGameInstances
        // Send the message.ts (timestamp) as the thread_ts parameter to reply as thread
        client.sendMessage(message.channel, s"<@${message.user}>: $reply", Some(message.ts))
      }
    }
  }

//  def messageUser(message: String, channel: String, thread_ts: Option[String], slackId: String): Unit = {
//    client.sendMessage(channel, s"<@$slackId>: $message", thread_ts)
//  }
}
