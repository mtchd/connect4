package connect4.commands

import connect4.Strings
import connect4.game.GameInstance

object CommandInterpreter {

  def interpret(message: String, authorId: String, gameInstance: Option[GameInstance]): (Option[GameInstance], Option[String]) = {
    gameInstance match {
      case Some(gameInstance) => interpretWithGame(message, authorId, gameInstance)
      case None => interpretWithoutGame(message, authorId)
    }
  }

  def interpretWithGame(message: String, authorId: String, gameInstance: GameInstance): (Option[GameInstance], Option[String]) = {
    message match {
      case CommandsRegex.Challenge(_, _, _) => wrapBoth(gameInstance, Strings.AlreadyGame)
      case CommandsRegex.Accept(_, flags) => wrapBoth.tupled(CommandHandler.accept(gameInstance, authorId, flags))
      case CommandsRegex.Drop(col) => wrapBoth.tupled(CommandHandler.drop(col.toInt, gameInstance, authorId))
      case CommandsRegex.Forfeit(_) => wrapReply.tupled(CommandHandler.forfeit(gameInstance, authorId))
      case CommandsRegex.Reject(_) => wrapReply.tupled(CommandHandler.reject(gameInstance, authorId))
      case CommandsRegex.Token(_*) => wrapBoth.tupled(CommandHandler.changeToken(gameInstance, message, authorId))
      case CommandsRegex.Help(_) => wrapBoth(gameInstance, Strings.Help)
      case _ => (Some(gameInstance), None)
    }
  }

  def interpretWithoutGame(message: String, authorId: String): (Option[GameInstance], Option[String]) = {
    message match {
      case CommandsRegex.Challenge(_, opponentId, flags) => wrapBoth.tupled(CommandHandler.challenge(opponentId, authorId, flags))
      case CommandsRegex.Accept(_, _) => (None, Some(Strings.FailedAcceptOrReject))
      case CommandsRegex.Drop(_) => (None, Some(Strings.FailedDrop))
      case CommandsRegex.Forfeit(_) => (None, Some(Strings.FailedForfeit))
      case CommandsRegex.Reject(_) => (None, Some(Strings.FailedAcceptOrReject))
      case CommandsRegex.Token(_, _, _) => (None, Some(Strings.NotInGame))
      case CommandsRegex.Help(_) => (None, Some(Strings.Help))
      case _ => (None, None)
    }
  }

  val wrapReply: (Option[GameInstance], String) => (Option[GameInstance], Option[String]) = (gameInstance, reply) => (gameInstance, Some(reply))
  val wrapBoth: (GameInstance, String) => (Option[GameInstance], Option[String]) = (gameInstance, reply) => (Some(gameInstance), Some(reply))

}
