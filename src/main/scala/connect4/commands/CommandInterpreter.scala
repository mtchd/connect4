package connect4.commands

import connect4.Strings
import connect4.game.GameInstance
import connect4.gamestore.ScoreStoreRow
import connect4.wrappers.Emoji

object CommandInterpreter {

  def bigBadInterpret(message: String): CommandPath = {
    message match {
      case CommandsRegex.Help(_) => NoContext(Help)
      case CommandsRegex.Score(_) => ScoreContext(PlayerScore)
      case CommandsRegex.Challenge(_, opponentId, flags) => GameAndScoreContext(Challenge(opponentId, flags))
      case CommandsRegex.Accept(_, flags) => GameAndScoreContext(Accept(flags))
      case CommandsRegex.Drop(col) => GameAndScoreContext(Drop(col))
      case CommandsRegex.Reject(_) => GameAndScoreContext(Reject)
      case CommandsRegex.Token(_) => GameAndScoreContext(Token(message))
      case CommandsRegex.Forfeit(_) => GameAndScoreContext(Forfeit)
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
  def interpretGameContextCommand(gameContextCommand: GameContextCommand, gameInstance: GameInstance, authorId: String, emojis: Vector[Emoji]): (GameInstance, String) = {
    gameContextCommand match {
        // TODO: Note this makes us do a redundant update to database
      case Challenge(_, _) => (gameInstance, Strings.AlreadyGame)
      case Accept(flags) => CommandHandler.accept(gameInstance, authorId, flags, emojis)
      case Drop(col) => CommandHandler.drop(col, gameInstance, authorId)
      case Reject => CommandHandler.reject(gameInstance, authorId)
      case Token(message) => CommandHandler.changeToken(gameInstance, message, authorId, emojis)
      case Forfeit => CommandHandler.forfeit(gameInstance, authorId)
    }
  }

}
