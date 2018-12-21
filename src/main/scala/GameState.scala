import Main.{boardCols, boardRows}

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
  def checkWin(): Option[Player] = {

    // TODO: Cleaner way of checking if last move exists.
    lastMove match {
      case Some(move) =>

        if (board.exists(x => recurseRow(x, move.player.token))) {
          Some(move.player)
        } else {
          None
        }

      case None =>
        None
    }
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

  def playMove(col: Int, player: Player): GameState = {

    // Get corresponding column
    val transposedBoard = board.transpose

    val move = findRow(transposedBoard(col), boardRows - 1, col, player)

    // TODO: This is messy and hard to read, please fix.
    // This is creating a new row with one character updated, then created a new board with one row updated.
    val newBoard = board.updated(move.row, board(move.row).updated(move.col, player.token))

    new GameState(newBoard, Some(move))
  }

  // TODO: Col and column are ambiguous, need better names.
  def findRow(column: List[Char], row: Int, col: Int, player: Player): Move = {

    if (column(row) == '-') {
      new Move(player, row, col)
    }
    else {
      findRow(column.tail, row-1, col, player)
    }

  }

}
