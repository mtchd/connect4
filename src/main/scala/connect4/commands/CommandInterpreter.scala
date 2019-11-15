package connect4.commands

import connect4.Strings
import connect4.game.GameInstance
import connect4.gamestore.ScoreStoreRow

sealed trait CommandPath
case class NoContext(noContextCommand: NoContextCommand) extends CommandPath
case class GameContext(gameContextCommand: GameContextCommand) extends CommandPath
case class ScoreContext(scoreContextCommand: ScoreContextCommand) extends CommandPath
case object NoReply extends CommandPath

sealed trait NoContextCommand
case object Help extends NoContextCommand

sealed trait ScoreContextCommand
case object PlayerScore extends ScoreContextCommand

sealed trait GameContextCommand
case class Challenge(opponentId: String, flags: String) extends GameContextCommand
case class Accept(flags: String) extends GameContextCommand
case class Drop(col: String) extends GameContextCommand
case object Reject extends GameContextCommand
case class Token(message: String) extends GameContextCommand
case object Forfeit extends GameContextCommand

object CommandInterpreter {

  def bigBadInterpret(message: String): CommandPath = {
    message match {
      case CommandsRegex.Help(_) => NoContext(Help)
      case CommandsRegex.Score(_) => ScoreContext(PlayerScore)
      case CommandsRegex.Challenge(_, opponentId, flags) => GameContext(Challenge(opponentId, flags))
      case CommandsRegex.Accept(_, flags) => GameContext(Accept(flags))
      case CommandsRegex.Drop(col) => GameContext(Drop(col))
      case CommandsRegex.Reject(_) => GameContext(Reject)
      case CommandsRegex.Token(_) => GameContext(Token(message))
      case CommandsRegex.Forfeit(_) => GameContext(Forfeit)
      case _ => NoReply
    }
  }

  def interpretNoContextCommand(noContextCommand: NoContextCommand): String = {
    noContextCommand match {
      case Help => Strings.Help
    }
  }

  def interpretScoreContextCommand(scoreContextCommand: ScoreContextCommand, score: ScoreStoreRow): String = {
    scoreContextCommand match {
      case PlayerScore => Strings.reportScore(score)
    }
  }

  // TODO: Author being in the game should be validated beforehand, ideally just pass role
  def interpretGameContextCommand(gameContextCommand: GameContextCommand, gameInstance: GameInstance, authorId: String): (GameInstance, String) = {
    gameContextCommand match {
        // TODO: Note this makes us do a redundant update to database
      case Challenge(_, _) => (gameInstance, Strings.AlreadyGame)
      case Accept(flags) => CommandHandler.accept(gameInstance, authorId, flags)
      case Drop(col) => CommandHandler.drop(col, gameInstance, authorId)
      case Reject => CommandHandler.reject(gameInstance, authorId)
      case Token(message) => CommandHandler.changeToken(gameInstance, message, authorId)
      case Forfeit => CommandHandler.forfeit(gameInstance, authorId)
    }
  }

}
