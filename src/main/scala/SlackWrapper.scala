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

    val client = SlackRtmClient(token, SlackApiClient.defaultSlackApiBaseUri, 10.seconds)

    var gameInstances: List[GameInstance] = List.empty

    println("Now listening to Slack...")

    client.onMessage { message =>

      val (newGameInstances, reply) = CommandHandler.interpret(message.text, message.user, gameInstances)
      gameInstances = newGameInstances
      println(message.ts)
      println(message.thread_ts)
      message.thread_ts match {
        case Some(thread_ts) => client.sendMessage(message.channel, s"<@${message.user}>: $reply", Some(thread_ts))
        case None => client.sendMessage(message.channel, s"<@${message.user}>: $reply", Some(message.ts))
      }

    }
  }

//  def messageUser(message: String, channel: String, thread_ts: Option[String], slackId: String): Unit = {
//    client.sendMessage(channel, s"<@$slackId>: $message", thread_ts)
//  }
}
