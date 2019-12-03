package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO}
import connect4.commands._
import connect4.gamestore.RDSGameStore
import connect4.wrappers.SlackIoHandler._
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack" /* config.getConfig("akka")*/)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
   * Start point of the program, handles all incoming messages in channels the bot is present in. Side effects be
   * here, but only here.
   */
  def startListening(slackRtmToken: String, dbPassword: String, slackApiToken: String)(implicit cs: ContextShift[IO]): Unit = {

    // TODO: Should we put the unsafeRunSyncs in the one for loop? How does threading work if we do that?
    val emojis: Vector[Emoji] = EmojiHandler.load(slackApiToken)
      .attempt
      .flatMap {
        case Right(emojis) => IO(emojis)
        // TODO: Worth stopping the program?
        case Left(e) => throw e
      }
      .unsafeRunSync()

    val rtmClient: SlackRtmClient = SlackRtmClient(slackRtmToken, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)
    val gameStore = RDSGameStore(dbPassword)

    gameStore.setupGameStore().unsafeRunSync()
    gameStore.setupScoreStore().unsafeRunSync()

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      val threadTs = message.thread_ts.getOrElse(message.ts)
      val messageContext = SendMessage(rtmClient, message, threadTs)

      val messageResponseProgram: IO[Any] = CommandInterpreter.bigBadInterpret(message.text) match {
        case NoReply => IO(Unit)
        case NoContext(command) => handleNoContextCommand(command, messageContext)
        case GameAndScoreContext(command) => handleGameAndScoreContextCommand(command, messageContext, gameStore, emojis)
        case ScoreContext(command) => handleScoreContextCommand(command, messageContext, gameStore)
      }

      messageResponseProgram
        .attempt
        .flatMap {
          // TODO: Inconsistent unit IOs
          case Right(_) => IO.unit
          case Left(e) => IO(e.printStackTrace())
        }
        .unsafeRunSync()

      }
    }
}