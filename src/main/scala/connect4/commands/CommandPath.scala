package connect4.commands

sealed trait CommandPath
case class NoContext(noContextCommand: NoContextCommand) extends CommandPath
case class GameAndScoreContext(gameContextCommand: GameContextCommand) extends CommandPath
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
