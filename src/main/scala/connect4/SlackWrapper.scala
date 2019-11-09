package connect4

import akka.actor.ActorSystem
import connect4.gamestore.{GameStore, InMemoryGameStore, LocalGameStore}
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack"/* config.getConfig("akka")*/)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in.
    */
  def startListening(token: String): Unit = {

    val rtmClient = SlackRtmClient(token, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      // Use information in message to *maybe* query database for relevant thread
      val thread = message.thread_ts.getOrElse(message.ts)

      // ThreadId => Vector[GameInstance]
      val gameInstances = LocalGameStore.get(thread)

      val (newGameInstances, reply) = CommandHandler.interpret(message.text, message.user, gameInstances)

      println(reply, newGameInstances, thread)
      // Persist state (ThreadId, Vector[GameInstance]) => Unit
      LocalGameStore.put(thread, newGameInstances)

      reply.foreach { replyText =>
        rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread))
      }

      //access dynamodb
    }
  }
}
