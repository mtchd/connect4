import Main.{boardCols, recurseRow}

/**
  * Represents a discrete state of the game.
  * @param board Connect 4 board, represented as characters for each cell.
  * @param lastMove Last move played, important for checking if game is won.
  */
class GameState(val board: List[String], lastMove: Option[Move]) {

  def printBoard(): Unit = {

    println("Game Board:")

    // Add column numbers above columns
    val annotatedBoard = "0123456789".take(boardCols) :: board

    for (line <- annotatedBoard) println(line)
  }

  // Search whole board for 4 Xs in a row or 4 Os in a row.
  // Most efficient implementation: Search around latest token for match
  // Other: Search only for Xs or Os at a time.
  def checkWin(): Boolean = {

    // TODO: Cleaner way of checking if last move exists.
    lastMove match {
      case Some(move) =>
        board.exists(x => recurseRow(x, move.player.token))
      case None =>
        // No move yet, just abort.
    }
  }

  def newBoard(boardRows: Int, boardCols: Int): GameState = {
    // TODO: This should be made into a function to take arbitrary board size, eg (5x7)
    // Starts at zero, of course.
    new GameState(List.fill(boardRows)("-"*boardCols), None)
  }
}
