import scala.annotation.switch
import scala.io.StdIn

object Main {

  val boardCols = 6
  val boardRows = 7

  // TODO: Divide into separate classes, instead of this...
  def main(args: Array[String]) {

    // Most things are hardcoded at this stage of the project, to be cleaned up.
    println("Welcome.")

    gameLoop(newBoard(boardRows, boardCols))

  }

  def printBoard(board : List[String]): Unit = {

    println("Game Board:")

    // Add column numbers above columns
    val annotatedBoard = "0123456789".take(boardCols) :: board

    for (line <- annotatedBoard) println(line)
  }

  // Could use error codes instead of manually entering error.
  def userError(message: String): Unit = {
    // Needs to tell user their input is an error, and return them to start of their turn.
    println(message)
  }

  // Go to the lowest row, see if you can insert, if not, go to row above, repeat.
  def updateBoard(board: List[String], move: Int, player: Char): List[String] = {

    // Handle 2nd base case, when we reach top of column
    if (board.isEmpty) {
      userError("Column is full.")
      return board
    }

    // "If" statement seems readable to me, but what is the standard here? Should I just keep it consistent?
    if (board.last(move) != '-')
      updateBoard(board.init, move, player) :+ board.last
    else board.init :+ board.last.updated(move, player)
  }

  def newBoard(boardRows: Int, boardCols: Int): List[String] = {
    // TODO: This should be made into a function to take arbitrary board size, eg (5x7)
    // Starts at zero, of course.
    List.fill(boardRows)("-"*boardCols)
  }

  def playTurn(board: List[String], player: Char): List[String] ={
    val move = StdIn.readLine("Enter column number:").toInt
    // Don't have to write return according to IDE, but I feel it is cleaner. What is the standard here?
    updateBoard(board, move, player)
  }

  // Search whole board for 4 Xs in a row or 4 Os in a row.
  // Most efficient implementation: Search around latest token for match
  // Other: Search only for Xs or Os at a time.
  def checkWin(board: List[String], player: Char): Boolean = {
    // Check horizontal
    board.exists(x => recurseRow(x, player))
  }

  // Return the cells for given col+row which need to be checked to verify if we
  // can have 4 in a row horizontally.
  def recurseRow(row: String, player: Char): Boolean = {

    val firstFour = row.take(4)

    if (firstFour.forall(x => x == player)) {
      true
    }
    else if (row.length > 4) {
      recurseRow(row.tail, player)
    }
    else {
      false
    }
  }


  def gameLoop(board: List[String]): Char = {

    printBoard(board)

    // Check if someone has won, if not, continue
    if (checkWin(board, 'X')) {
      println("Player X Wins")
      return 'X'
    }
    else if (checkWin(board, 'O')) {
      println("Player O Wins")
      return 'O'
    }

    val command = parseInput(StdIn.readLine("Enter Command:"))

    (command: @switch) match {
      case 0 => userError("Invalid input.") ; gameLoop(board)
      // Ideally, players are a proper object and not just a character, but that is to come.
      case 1 => gameLoop(playTurn(board, 'X'))
      case 2 => gameLoop(playTurn(board, 'O'))
      case 3 => gameLoop(newBoard(boardRows, boardCols))
      // Return '-' to signify draw.
      case 4 => '-'
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