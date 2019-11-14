package connect4.commands

import connect4.Strings
import connect4.game.GameInstance

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

  def interpretScoreContextCommand(scoreContextCommand: ScoreContextCommand): String = {
    scoreContextCommand match {
      case PlayerScore => "You asked for your score!"
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

  def interpret(message: String, authorId: String, gameInstance: Option[GameInstance]): (Option[GameInstance], Option[String]) = {

    gameInstance match {
      case Some(gameInstance) => interpretWithGame(message, authorId, gameInstance)
      case None => interpretWithoutGame(message, authorId)
    }
  }

  def interpretWithGame(message: String, authorId: String, gameInstance: GameInstance): (Option[GameInstance], Option[String]) = {
    message match {
      case CommandsRegex.Challenge(_, _, _) => wrapReply(None, Strings.AlreadyGame)
      case CommandsRegex.Accept(_, flags) => wrapBoth.tupled(CommandHandler.accept(gameInstance, authorId, flags))
      case CommandsRegex.Drop(col) => wrapBoth.tupled(CommandHandler.drop(col, gameInstance, authorId))
      case CommandsRegex.Forfeit(_) => wrapBoth.tupled(CommandHandler.forfeit(gameInstance, authorId))
      case CommandsRegex.Reject(_) => wrapBoth.tupled(CommandHandler.reject(gameInstance, authorId))
      case CommandsRegex.Token(_) => wrapBoth.tupled(CommandHandler.changeToken(gameInstance, message, authorId))
      case CommandsRegex.Help(_) => wrapReply(None, Strings.Help)
      case _ => (None, None)
    }
  }

  def interpretWithoutGame(message: String, authorId: String): (Option[GameInstance], Option[String]) = {
    message match {
      case CommandsRegex.Challenge(_, opponentId, flags) => wrapBoth.tupled(CommandHandler.challenge(opponentId, authorId, flags))
      case CommandsRegex.Accept(_, _) => (None, Some(Strings.FailedAcceptOrReject))
      case CommandsRegex.Drop(_) => (None, Some(Strings.FailedDrop))
      case CommandsRegex.Forfeit(_) => (None, Some(Strings.FailedForfeit))
      case CommandsRegex.Reject(_) => (None, Some(Strings.FailedAcceptOrReject))
      case CommandsRegex.Token(_) => (None, Some(Strings.NotInGame))
      case CommandsRegex.Help(_) => (None, Some(Strings.Help))
      case _ => (None, None)
    }
  }

  val wrapReply: (Option[GameInstance], String) => (Option[GameInstance], Option[String]) = (gameInstance, reply) => (gameInstance, Some(reply))
  val wrapBoth: (GameInstance, String) => (Option[GameInstance], Option[String]) = (gameInstance, reply) => (Some(gameInstance), Some(reply))

}
