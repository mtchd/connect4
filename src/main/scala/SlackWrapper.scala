import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
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

    var threadAndGameInstances: Map[String, List[GameInstance]] = Map.empty

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      val thread = message.thread_ts.getOrElse(message.ts)

      val gameInstances = threadAndGameInstances.getOrElse(thread, List.empty)

      val (newGameInstances, reply) = CommandHandler.interpret(message.text, message.user, gameInstances)

      threadAndGameInstances = threadAndGameInstances.updated(thread, newGameInstances)

      reply.foreach { replyText =>
        rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread))
      }
    }
  }
}
