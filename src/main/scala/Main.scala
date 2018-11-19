import scala.io.StdIn

object Main {
  def main(args: Array[String]) {

    // Most things are hardcoded at this stage of the project, to be cleaned up.
    println("Welcome.")

    // This should be made into a function the take arbitrary board size, eg (5x7)
    // Starts at zero, of course.
    val board = "012345" :: List.fill(7)("------")
    printBoard(board)

    gameLoop(board)

  }

  def printBoard(board : List[String]): Unit = {
    println("Game Board:")
    for (line <- board) println(line)
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

  def playTurn(board: List[String], player: Char): List[String] ={
    val move = StdIn.readLine("Enter column number:").toInt
    val new_board = updateBoard(board, move, player)
    printBoard(new_board)
    // Don't have to write return according to IDE, but I feel it is cleaner. What is the standard here?
    new_board
  }

  def checkWin(board: List[String]): Boolean = {
    true
  }

  def gameLoop(board: List[String]): Char= {

    // Take in a command specifying if you want to quit, restart, play X, or play 0.
    val new_board0 = playTurn(board, 'X')
    val new_board1 = playTurn(new_board0, 'O')
    gameLoop(new_board1)
  }

  /**
    * Takes command line input and returns a code corresponding to the command.
    * @param input Command entered by user.
    * @return Code indicating what to execute.
    */
  def parseInput(input: String): Int = {
    0
  }
}