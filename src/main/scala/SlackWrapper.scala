import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = SlackRtmClient(ConfigFactory.load().getString("secrets.slackApiKey"))
  val selfId: String = client.state.self.id

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in.
    */
  // TODO: Divide the command parsing and the side effects up
  def startListening(): Unit = {

    client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      if(mentionedIds.contains(selfId)) {

        message.text match {
          case _ => client.sendMessage(message.channel, s"<@${message.user}>: ${Strings.Help}")
        }
      }
    }
  }

  // TODO: Something has to be bad about this aggressive overloading
  def messageUser(message: String, channel: String, thread_ts: Option[String], slackId: String): Unit = {
    client.sendMessage(channel, s"<@$slackId>: $message", thread_ts)
  }
}
