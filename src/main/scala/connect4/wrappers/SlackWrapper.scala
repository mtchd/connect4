package connect4.wrappers

import akka.actor.ActorSystem
import connect4.commands.CommandInterpreter
import connect4.gamestore.{GameStoreRow, RDSGameStore}
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack"/* config.getConfig("akka")*/)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in.
    */
  def startListening(slackToken: String, password: String): Unit = {

    val rtmClient = SlackRtmClient(slackToken, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)
    val gameStore = RDSGameStore(password)

    gameStore.setup().unsafeRunSync()

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      // Use information in message to *maybe* query database for relevant thread
      val thread = message.thread_ts.getOrElse(message.ts)

      // ThreadId => IO[Option[GameInstance]]
      val getIo = gameStore.get(thread)
      val maybeGameRow: Option[GameStoreRow] = getIo.unsafeRunSync()
      val maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

      val (newMaybeGameInstance, reply) = CommandInterpreter.interpret(message.text, message.user, maybeGameInstance)

      reply.foreach { replyText =>
        rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread))
      }

      //access dynamodb
    }
  }
}
