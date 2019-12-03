package connect4.wrappers

import cats.effect.IO
import connect4.Strings
import connect4.commands.{Challenge, CommandHandler, CommandInterpreter, GameContextCommand, NoContextCommand, ScoreContextCommand}
import connect4.game.{Finished, GameInstance, Ranked, UnRanked}
import connect4.gamestore.RDSGameStore
import slack.models.Message
import slack.rtm.SlackRtmClient
import cats.implicits._

case class SendMessage(rtmClient: SlackRtmClient, message: Message, thread: String)

// TODO: Uncouple replying
// TODO: Rewrite as a case class, store clients / gamestore
object SlackIoHandler {

  def handleGame(gameInstance: GameInstance, command: GameContextCommand, gameStore: RDSGameStore, sendMessage: SendMessage, emojis: Vector[Emoji]): IO[Unit] = {

    val (newGameInstance, reply) = CommandInterpreter.interpretGameContextCommand(command, gameInstance, sendMessage.message.user, emojis)
    val gamePutIo = putGameAndReplyIo(sendMessage, reply, gameStore, newGameInstance)
    newGameInstance match {
      case Finished(rankType) => rankType match {
        case UnRanked => gamePutIo
        case Ranked(winnerId, loserId) => updateScores(winnerId, loserId, gameStore, gamePutIo, sendMessage)
      }
      case _ => gamePutIo
    }

  }

  def handleChallenge(defenderId: String, flags: String, gameStore: RDSGameStore, sendMessage: SendMessage, emojis: Vector[Emoji]): IO[Unit] = {

    val (newGameInstance, reply) = CommandHandler.challenge(defenderId, sendMessage.message.user, flags, emojis)
    putGameAndReplyIo(sendMessage, reply, gameStore, newGameInstance)

  }

  def putGameAndReplyIo(sendMessage: SendMessage, reply: String, gameStore: RDSGameStore, gameInstance: GameInstance): IO[Unit] = {

    for {
      _ <- SlackIoHandler.reply(sendMessage, reply)
      _ <- gameStore.put(sendMessage.thread, gameInstance)
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

  // TODO: This could be less nested and coupled
  def handleGameAndScoreContextCommand(command: GameContextCommand, sendMessage: SendMessage, gameStore: RDSGameStore, emojis: Vector[Emoji]): IO[Unit] = {
    for {
      maybeGameRow <- gameStore.get(sendMessage.thread)
      maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

      _ <- maybeGameInstance match {
        case Some(gameInstance) => handleGame(gameInstance, command, gameStore, sendMessage, emojis)
        case None => command match {
          case Challenge(opponentId, flags) => handleChallenge(opponentId, flags, gameStore, sendMessage, emojis)
          case _ => reply(sendMessage, Strings.NotInGame)
        }
      }
    } yield ()
  }

}
