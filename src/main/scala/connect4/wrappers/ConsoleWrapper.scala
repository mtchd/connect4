package connect4.wrappers

import connect4._
import connect4.commands.{CommandHandler, CommandsRegex}
import connect4.game.{CellContents, Challenger, Defender, GameState, PlayerRole}

object ConsoleWrapper {

  def startListening(): Unit = {
    val gameState = GameState.newDefaultBoard()
    println("Game ready")
    listen(gameState)
  }

  @scala.annotation.tailrec
  private def listen(gameState: GameState): Unit = {

    val playerRole = askRole()

    println(s"You are $playerRole. Enter Command:")
    val input = scala.io.StdIn.readLine()

    val (newGameState, reply) = interpretCommand(gameState, playerRole, input)

    println(reply)

    listen(newGameState)
  }

  @scala.annotation.tailrec
  private def askRole(): PlayerRole = {
    println("\nEnter 'D' for defender or 'C' for challenger:")

    val input = scala.io.StdIn.readLine()

    input match {
      case CommandsRegex.ChallengerRole(_) => Challenger
      case CommandsRegex.DefenderRole(_) => Defender
      case _ => askRole()
    }
  }

  private def interpretCommand(gameState: GameState, playerRole: PlayerRole, input: String): (GameState, String) = {

    input match {

      case CommandsRegex.Drop(_,col,_) => {
        val (newGameState, reply) = CommandHandler.play(
          col.toInt,
          gameState,
          playerRole
        )
        (newGameState, reply + "\n" + newGameState.boardAsConsoleString())
      }

      case CommandsRegex.Forfeit(_) => (GameState.newDefaultBoard(), Strings.Forfeit + "\n" + "Starting new game...")

      case _ => (gameState, Strings.InGameCommands)
    }

  }

}
