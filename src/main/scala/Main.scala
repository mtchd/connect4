import akka.actor.ActorSystem

import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.annotation.switch
import scala.io.StdIn
import scala.concurrent.ExecutionContextExecutor

import com.typesafe.config.ConfigFactory

object Main {

  // Hardcoded crap
  val nBoardCols = 6
  val nBoardRows = 7

  // Hardcoded for now
  val emptySpace = "⚪"
  val emptySpaceC = '⚪'

  val playerX = new Player('❌')
  val playerO = new Player('O')

  // TODO: Classes for more things, such as cells, then have rows/cols as List[Cell]
  // TODO: Tests
  def main(args: Array[String]) {

    SlackClient.startListening()

    // Starts terminal version of game
    gameLoop(new GameState(nBoardRows, nBoardCols))

  }

  // Could use error codes instead of manually entering error.
  def userError(message: String): Unit = {
    // Needs to tell user their input is an error, and return them to start of their turn.
    println(message)
  }

  def playTurn(gameState: GameState, player: Player): GameState ={

    val col = StdIn.readLine("Enter column number:").toInt

    // Check col within bounds (0 to number of columns take 1)
    if (col >= 0 && col < nBoardCols) {
      gameState.playMove(col, player)
    }
    else {
      userError("Column out of bounds. Try again.")
      playTurn(gameState, player)
    }

  }

  def gameLoop(gameState: GameState): Option[Player] = {

    gameState.printBoard()

    // Check if someone has won, and finish game if so.
    val winner = gameState.checkWin()
    if (winner.isDefined) {
      val winningPlayer = winner.get
      println(winningPlayer.token + " wins!")
      // Finish game
      return Some(winningPlayer)
    }

    val command = parseInput(StdIn.readLine("Enter Command:"))

    (command: @switch) match {
      case 0 => userError("Invalid input.") ; gameLoop(gameState)
      // Ideally, players are a proper object and not just a character, but that is to come.
      case 1 => gameLoop(playTurn(gameState, playerX ))
      case 2 => gameLoop(playTurn(gameState, playerO))
      case 3 => gameLoop(new GameState(nBoardRows, nBoardCols))
      // Return none to signify draw.
      case 4 => None
    }

  }

  /**
    * Takes command line input and returns a code corresponding to the command.
    * This uncouples command inputs, so it can be recomposed later for Slack.
    * @param input Command entered by user.
    * @return Code indicating what to execute.
    */
  def parseInput(input: String):  Int = {

    (input: @switch) match {
      case "X" => 1
      case "O" => 2
      case "reset" => 3
      case "exit" => 4
      case _ => 0
    }

  }
}