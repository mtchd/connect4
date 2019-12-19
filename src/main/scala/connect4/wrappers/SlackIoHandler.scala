package connect4.wrappers

import cats.effect.IO
import connect4.Strings
import connect4.commands.{Challenge, CommandHandler, CommandInterpreter, GameContextCommand, NoContextCommand, ScoreContextCommand}
import connect4.game.{CellContents, Finished, GameInstance, PlayerRole, Ranked, UnFinishedGame, UnRanked}
import connect4.gamestore.RDSGameStore
import slack.models.Message
import slack.rtm.SlackRtmClient
import cats.implicits._

case class MessageContext(rtmClient: SlackRtmClient, message: Message, thread: String)

// TODO: Uncouple replying
case class SlackIoHandler(gameStore: RDSGameStore, emojiHandler: EmojiHandler) {

  def handleGame(gameInstance: UnFinishedGame, command: GameContextCommand, messageContext: MessageContext, playerRole: PlayerRole): IO[Unit] = {

    val (newGameInstance, reply) = CommandInterpreter.interpretGameContextCommand(command, gameInstance, messageContext.message.user, emojiHandler, playerRole)
    val gamePutIo = putGameAndReplyIo(messageContext, reply, newGameInstance)
    newGameInstance match {
      case Finished(rankType) => rankType match {
        case UnRanked => gamePutIo
        case Ranked(winnerId, loserId) => updateScores(winnerId, loserId, gamePutIo, messageContext)
      }
      case _ => gamePutIo
    }

  }

  def handleChallenge(defenderId: String, flags: String, messageContext: MessageContext): IO[Unit] = {
    val (newGameInstance, reply) = CommandHandler.challenge(defenderId, messageContext.message.user, flags, emojiHandler)
    putGameAndReplyIo(messageContext, reply, newGameInstance)
  }

  def putGameAndReplyIo(messageContext: MessageContext, replyText: String, gameInstance: GameInstance): IO[Unit] =
    for {
      _ <- reply(messageContext, replyText)
      _ <- gameStore.put(messageContext.thread, gameInstance)
    } yield ()

  def updateScores(winnerId: String, loserId: String, gamePutIo: IO[Unit], messageContext: MessageContext): IO[Unit] =
    for {
      _ <- gamePutIo
      _ <- gameStore.updateLoss(loserId)
      _ <- gameStore.updateWin(winnerId)
      scores <- gameStore.reportScores(winnerId, loserId)
      _ <- scores.traverse_ { score => reply(messageContext, score.toString) }
    } yield ()

  def handleNoContextCommand(command: NoContextCommand, messageContext: MessageContext): IO[Unit] = {
    val replyText = CommandInterpreter.interpretNoContextCommand(command)
    reply(messageContext, replyText)
  }

  def handleScoreContextCommand(command: ScoreContextCommand, messageContext: MessageContext): IO[Unit] =
    for {
      maybeScore <- gameStore.reportScore(messageContext.message.user)
      _ <- maybeScore match {
        case Some(score) => reply(messageContext, CommandInterpreter.interpretScoreContextCommand(command, score))
        case None => reply(messageContext, Strings.HaventPlayed)
      }
    } yield ()

  def reply(messageContext: MessageContext, replyText: String): IO[Unit] =
    IO(messageContext.rtmClient.sendMessage(
      messageContext.message.channel,
      Strings.atUser(messageContext.message.user, replyText),
      Some(messageContext.thread)))

  // TODO: This could be less nested and coupled
  def handleGameAndScoreContextCommand(command: GameContextCommand, messageContext: MessageContext): IO[Unit] =
    for {
      maybeGameInstance <- gameStore.maybeGetGame(messageContext.thread)

      _ <- maybeGameInstance match {
        case Some(gameInstance) => checkPlayerInGameThenHandle(gameInstance, command, messageContext)
        case None => command match {
          case Challenge(opponentId, flags) => handleChallenge(opponentId, flags, messageContext)
          case _ => reply(messageContext, Strings.NotInGame)
        }
      }
    } yield ()

  // TODO: Not necessarily slack IO
  def checkPlayerInGameThenHandle(gameInstance: UnFinishedGame, command: GameContextCommand, messageContext: MessageContext): IO[Unit] =
    gameInstance.maybePlayerRole(messageContext.message.user) match {
      case Some(playerRole) => handleGame(gameInstance, command, messageContext, playerRole)
        // TODO: Say who is in the game and what they need to do (based on challenge vs playing)
      case None => reply(messageContext, Strings.NotInGame)
    }

}
