package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.IO
import cats.implicits._
import connect4.Strings
import connect4.commands.{Challenge, CommandHandler, CommandInterpreter, GameContext, GameContextCommand, Help, NoContext, NoContextCommand, NoReply, ScoreContext, ScoreContextCommand}
import connect4.game.{Finished, GameInstance, Ranked, UnRanked}
import connect4.gamestore.{GameStoreRow, RDSGameStore, ScoreStoreRow}
import slack.api.SlackApiClient
import slack.models.Message
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
  def startListening(slackToken: String, password: String): Unit = {

    val rtmClient = SlackRtmClient(slackToken, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)
    val gameStore = RDSGameStore(password)

    gameStore.setupGameStore().unsafeRunSync()
    gameStore.setupScoreStore().unsafeRunSync()

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      val threadTs = message.thread_ts.getOrElse(message.ts)
      val messageContext = SendMessage(rtmClient, message, threadTs)

      val messageResponseProgram: IO[Any] = CommandInterpreter.bigBadInterpret(message.text) match {
        case NoReply => IO(Unit)
        case NoContext(command) => handleNoContextCommand(command, messageContext)
        case GameContext(command) => handleGameContextCommand(command, messageContext, gameStore)
        case ScoreContext(command) => handleScoreContextCommand(command, messageContext, gameStore)
      }

      val messageResponseProgram = for {
        // Use information in message to *maybe* query database for relevant thread
        thread <- IO(message.thread_ts.getOrElse(message.ts))

        // ThreadId => IO[Option[GameInstance]]
        maybeGameRow <- gameStore.get(thread)
        maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

        (newMaybeGameInstance, reply) = CommandInterpreter.interpret(message.text, message.user, maybeGameInstance)

        _ <- reply.traverse_ { replyText =>
          IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread)))
        }

        _ <- putGameInstance(newMaybeGameInstance, gameStore, thread)

        _ <- updateScoreStoreWithLoss(newMaybeGameInstance, gameStore)

        _ <- updateScoreStoreWithWin(newMaybeGameInstance, gameStore)

        scores <- getScores(newMaybeGameInstance, gameStore)

        _ <- scores.traverse_ { score =>
          IO(rtmClient.sendMessage(message.channel, s"$score", Some(thread)))
        }

      //access dynamodb
    }
  }
  // TODO: Dear god loss input please
  def handleGame(gameInstance: GameInstance, command: GameContextCommand, authorId: String, rtmClient: SlackRtmClient, message: Message, gameStore: RDSGameStore, thread: String): IO[Unit] = {

    val (newGameInstance, reply) = CommandInterpreter.interpretGameContextCommand(command, gameInstance, sendMessage.message.user)
    val gamePutIo = putGameAndReplyIo(sendMessage, reply, gameStore, newGameInstance)
    newGameInstance match {
      case Finished(rankType) => rankType match {
        case UnRanked => gamePutIo
        case Ranked(winnerId, loserId) => updateScores(winnerId, loserId, gameStore, gamePutIo, sendMessage)
      }
      case _ => gamePutIo
    }

  }

  def handleChallenge(defenderId: String, flags: String, gameStore: RDSGameStore, sendMessage: SendMessage): IO[Unit] = {

    val (newGameInstance, reply) = CommandHandler.challenge(defenderId, sendMessage.message.user, flags)
    putGameAndReplyIo(sendMessage, reply, gameStore, newGameInstance)

  }

  def putGameAndReplyIo(sendMessage: SendMessage, reply: String, gameStore: RDSGameStore, gameInstance: GameInstance): IO[Unit] = {

    // TODO: Do something about this unpacking
    val rtmClient = sendMessage.rtmClient
    val message = sendMessage.message
    val thread = sendMessage.thread

    for {
      _ <- IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $reply", Some(thread)))
      _ <- gameStore.put(thread, gameInstance)
    } yield ()
  }

  def updateScores(winnerId: String, loserId: String, gameStore: RDSGameStore, gamePutIo: IO[Unit], sendMessage: SendMessage): IO[Unit] = {
    for {
      _ <- gamePutIo
      _ <- gameStore.updateLoss(loserId)
      _ <- gameStore.updateWin(winnerId)
      scores <- gameStore.reportScores(winnerId, loserId)
      _ <- scores.traverse_ { score => reply(sendMessage, score.toString) }
    } yield ()
  }

  def handleNoContextCommand(command: NoContextCommand, sendMessage: SendMessage): IO[Unit] = {
    val replyText = CommandInterpreter.interpretNoContextCommand(command)
    reply(sendMessage, replyText)
  }

  def handleScoreContextCommand(command: ScoreContextCommand, sendMessage: SendMessage, gameStore: RDSGameStore): IO[Unit] = {

    for {
      maybeScore <- gameStore.reportScore(sendMessage.message.user)

      _ <- maybeScore match {
        case Some(score) => reply(sendMessage, CommandInterpreter.interpretScoreContextCommand(command, score))
        case None => reply(sendMessage, Strings.HaventPlayed)
      }

    } yield ()

  }

  def reply(sendMessage: SendMessage, replyText: String): IO[Unit] = {
    IO(sendMessage.rtmClient.sendMessage(
      sendMessage.message.channel,
      Strings.atUser(sendMessage.message.user, replyText),
      Some(sendMessage.thread)))
  }

  def handleGameContextCommand(command: GameContextCommand, sendMessage: SendMessage, gameStore: RDSGameStore): IO[Unit] = {
    for {
      maybeGameRow <- gameStore.get(sendMessage.thread)
      maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

      _ <- maybeGameInstance match {
        case Some(gameInstance) => handleGame(gameInstance, command, gameStore, sendMessage)
        case None => command match {
          case Challenge(opponentId, flags) => handleChallenge(opponentId, flags, gameStore, sendMessage)
          case _ => reply(sendMessage, Strings.NotInGame)
        }
      }
    } yield ()
  }
}

case class SendMessage(rtmClient: SlackRtmClient, message: Message, thread: String)