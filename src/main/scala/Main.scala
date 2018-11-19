import scala.io.StdIn

object Main {
  def main(args: Array[String]) {

    println("Welcome.")

    // This should be made into a function the take arbitrary board size, eg (5x7)
    // Starts at zero, of course.
    val board = "012345" :: List.fill(7)("------")
    printBoard(board)

    // This will be the start of the "game loop"
    val new_board = playTurn(board)
    playTurn(new_board)

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
  def updateBoard(board: List[String], move: Int): List[String] = {

    // Handle 2nd base case, when we reach top of column
    if (board.isEmpty) {
      userError("Column is full.")
      return board
    }

    // "If" statement seems readable to me, but what is the standard here? Should I just keep it consistent?
    if (board.last(move) == 'X')
      updateBoard(board.init, move) :+ board.last
    else board.init :+ board.last.updated(move,'X')
  }

  def playTurn(board: List[String]): List[String] ={
    val move = StdIn.readLine("Enter column number:").toInt
    val new_board = updateBoard(board, move)
    printBoard(new_board)
    // Don't have to write return according to IDE, but I feel it is cleaner. What is the standard here?
    new_board
  }
}