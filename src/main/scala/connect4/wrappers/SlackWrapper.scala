package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO}
import connect4.commands._
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack" /* config.getConfig("akka")*/)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  // Side effects are here, but only here.
  def startListening(slackRtmToken: String, dbPassword: String, slackApiToken: String)(implicit cs: ContextShift[IO]): Unit = {

    // First side effect, loads program (gets external info like emojis)
    val slackIoHandler = SlackIoHandler.attemptLoad(slackApiToken, dbPassword).unsafeRunSync()
    val rtmClient: SlackRtmClient = SlackRtmClient(slackRtmToken, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)

    rtmClient.onMessage { message =>

      val threadTs = message.thread_ts.getOrElse(message.ts)
      val messageContext = MessageContext(rtmClient, message, threadTs)

      val messageResponseProgram: IO[Any] = CommandInterpreter.interpretMessage(message.text) match {
        case NoReply => IO(Unit)
        case NoContext(command) => slackIoHandler.handleNoContextCommand(command, messageContext)
        case GameAndScoreContext(command) => slackIoHandler.handleGameAndScoreContextCommand(command, messageContext)
        case ScoreContext(command) => slackIoHandler.handleScoreContextCommand(command, messageContext)
      }

      // Second side effect, responds to users
      messageResponseProgram
        .attempt
        .flatMap {
          case Right(_) => IO(Unit)
          case Left(e) => IO(e.printStackTrace())
        }
        .unsafeRunSync()

      }
    }
}