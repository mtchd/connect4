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

  implicit val system: ActorSystem = ActorSystem("slack"/* config.getConfig("akka")*/)
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
        case ScoreContext(command) => handleScoreContextCommand(command, messageContext)
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

    putarydoo(sendMessage, reply, gameStore, newGameInstance)

  }

  // TODO: Dear god less input please
  def handleChallenge(challengerId: String, defenderId: String, flags: String, gameStore: RDSGameStore, sendMessage: SendMessage): IO[Unit] = {

    val (newGameInstance, reply) = CommandHandler.challenge(challengerId, defenderId, flags)
    val io = putarydoo(sendMessage, reply, gameStore, newGameInstance)

    newGameInstance match {
      case Finished(rankType) => {
        rankType match {
          case UnRanked => io
          case Ranked(winnerId, loserId) => updateScores(winnerId, loserId, gameStore, io, message, rtmClient, thread)
        }
      }
      case _ => io
    }

  }

  // TODO: Better name
  def putarydoo(sendMessage: SendMessage, reply: String, gameStore: RDSGameStore, gameInstance: GameInstance): IO[Unit] = {

    // TODO: Do something about this unpacking
    val rtmClient = sendMessage.rtmClient
    val message = sendMessage.message
    val thread = sendMessage.thread

    for {
      _ <- IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $reply", Some(thread)))
      _ <- gameStore.put(thread, gameInstance)
    } yield ()
  }

  def updateScores(winnerId: String, loserId: String, gameStore: RDSGameStore, io: IO[Unit], sendMessage: SendMessage): IO[Unit] = {
    for {
      _ <- gameStore.updateLoss(loserId)
      _ <- gameStore.updateWin(winnerId)
      scores <- gameStore.reportScores(winnerId, loserId)
      _ <- scores.traverse_ { score =>
        reply(sendMessage, score.toString)
      }
    } yield ()
  }

  def handleNoContextCommand(command: NoContextCommand, sendMessage: SendMessage): IO[Unit] = {
    val replyText = CommandInterpreter.interpretNoContextCommand(command)
    // TODO: Unreadable garbage
    reply(sendMessage, replyText)
  }

  def handleScoreContextCommand(command: ScoreContextCommand, sendMessage: SendMessage): IO[Unit] = {
    val replyText = CommandInterpreter.interpretScoreContextCommand(command)
    // TODO: Unreadable garbage
    reply(sendMessage, replyText)
  }

  def reply(sendMessage: SendMessage, replyText: String): IO[Unit] = {
    IO(sendMessage.rtmClient.sendMessage(sendMessage.message.channel, s"<@${sendMessage.message.user}>: $replyText", Some(sendMessage.thread)))
  }

  def handleGameContextCommand(command: GameContextCommand, sendMessage: SendMessage, gameStore: RDSGameStore): IO[Unit] = {

    // TODO: Do something about unpacking
    val thread = sendMessage.thread
    val message = sendMessage.message
    val rtmClient = sendMessage.rtmClient

    for {
      maybeGameRow <- gameStore.get(thread)
      maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

      _ <- maybeGameInstance match {
        case Some(gameInstance) => handleGame(gameInstance, command, gameStore, sendMessage) // Continue
        case None => {
          command match {
            case Challenge(opponentId, flags) => handleChallenge(message.user, opponentId, flags, rtmClient, message, gameStore, thread)
          