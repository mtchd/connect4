import scala.io.StdIn

object Main {
  def main(args: Array[String]) {

    println("Welcome.")

    // This should be made into a function the take arbitrary board size, eg (5x7)
    val board = "123456" :: List.fill(7)("------")
    printBoard(board)

    // This will be the start of the "game loop"
    val move = StdIn.readLine("Enter column number:").toInt - 1

    // Go to the lowest row, see if you can insert, if not, go to row above, repeat.
    // Currently messy implementation, to be updated.
    val new_board = board.take(7) :+ board.last.updated(move,"X").mkString("")

    printBoard(new_board)

    // Implement way of checking board.
  }

  def printBoard(board : List[String]): Unit = {
    println("Game Board:")
    for (line <- board) println(line)
  }
}