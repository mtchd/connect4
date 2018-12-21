import scala.annotation.switch
import scala.io.StdIn

object Main {

  val boardCols = 6
  val boardRows = 7

  val playerX = new Player('X')
  val playerO = new Player('O')

  // TODO: Classes for more things, such as cells, then have rows/cols as List[Cell}
  // TODO: Tests
  def main(args: Array[String]) {

    // Most things are hardcoded at this stage of the project, to be cleaned up.
    println("Welcome.")

    gameLoop(newBoard(boardRows, boardCols))

  }

  // Could use error codes instead of manually entering error.
  def userError(message: String): Unit = {
    // Needs to tell user their input is an error, and return them to start of their turn.
    println(message)
  }

  def newBoard(boardRows: Int, boardCols: Int): GameState = {
    // Starts at zero, of course.
    new GameState(List.fill(boardRows)("-"*boardCols), None)
  }

  def playTurn(gameState: GameState, player: Player): GameState ={
    val col = StdIn.readLine("Enter column number:").toInt
    // Don't have to write return according to IDE, but I feel it is cleaner. What is the standard here?
    gameState.playMove(col, player)
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
      case 3 => gameLoop(newBoard(boardRows, boardCols))
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