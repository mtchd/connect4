import Main.{userError, emptySpace, emptySpaceC}

/**
  * Represents a discrete state of the game.
  * @param board Connect 4 board, represented as characters for each cell.
  * @param lastMove Last move played, important for checking if game is won.
  */
class GameState(val board: List[String], lastMove: Option[Move]) {

  // Extract number of columns and rows
  // More efficient to pass in class constructor, but this keeps code a little cleaner I think.
  val nBoardCols: Int = board.head.length
  val nBoardRows: Int = board.length

  // For constructing brand new board
  def this(boardRows: Int, boardCols: Int) = this(List.fill(boardRows)(emptySpace*boardCols), None)

  def printBoard(): Unit = {

    println("Game Board:")

    // Add column numbers above columns
    val annotatedBoard = "0123456789".take(nBoardCols) :: board

    for (line <- annotatedBoard) println(line)
  }

  def boardAsString(): String = {
    // Need to multiply boardCols by 2 because the emojis are two characters each
    "\nGame Board:\n" + "0⃣1⃣2⃣3⃣4⃣5⃣6⃣7⃣8⃣9⃣".take(nBoardCols*2) + "\n" + board.mkString("\n")
  }

  // Search whole board for 4 Xs in a row or 4 Os in a row.
  // Most efficient implementation: Search around latest token for match
  // Other: Search only for Xs or Os at a time.
  def checkWin(): Option[Player] = {

    // TODO: Cleaner way of checking if last move exists.
    lastMove match {
      case Some(move) =>

        // Had to extract this here due to list access syntax being same as function syntax, i.e you can't put brackets
        // after transpose otherwise it thinks you're feeding it variables.
        val transposedBoard = board.transpose

        if (fourInARow(board(move.row), move.player.token) ||
            fourInARow(transposedBoard(move.col).mkString, move.player.token) ||
            fourInARow(getSEDiagonal(move.row, move.col, -3), move.player.token) ||
            fourInARow(getNEDiagonal(move.row, move.col, -3), move.player.token))
        {
          Some(move.player)
        }
        else
        {
          None
        }

      case None =>
        None
    }
  }

  // Return the cells for given col+row which need to be checked to verify if we
  // can have 4 in a row horizontally.
  def fourInARow(row: String, player: Char): Boolean = {

    val firstFour = row.take(4)

    if (firstFour.forall(x => x == player)) {
      true
    }
    else if (row.length > 4) {
      fourInARow(row.tail, player)
    }
    else {
      false
    }
  }

  // TODO: Rediscover that algorithm for diagonals instead of this manual stuff
  // Get southeast diagonal
  def getSEDiagonal(row: Int, col: Int, offset: Int): String = {

    if (offset == 3) {
      safeGet(row + offset, col + offset)
    }
    else {
     safeGet(row + offset, col + offset) + getSEDiagonal(row, col, offset + 1)
    }
  }

  // Get southeast diagonal
  def getNEDiagonal(row: Int, col: Int, offset: Int): String = {

    if (offset == 3) {
      safeGet(row - offset, col + offset)
    }
    else {
      safeGet(row - offset, col + offset) + getSEDiagonal(row, col, offset + 1)
    }
  }


  // This could be better by using option, as returning '-' means there is ambiguity between free spaces and out of
  // bounds spaces
  def safeGet(row: Int, col: Int): String = {
    try {
      board(row)(col).toString
    } catch {
      case e: IndexOutOfBoundsException => emptySpace
    }
  }

  def playMove(col: Int, player: Player): GameState = {

    // Get corresponding column
    val transposedBoard = board.transpose

    val move = findRow(transposedBoard(col), nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      userError("Column is full.")
      return this
    }

    // TODO: This is messy and hard to read, please fix.
    // This is creating a new row with one character updated, then created a new board with one row updated.
    val newBoard = board.updated(move.row, board(move.row).updated(move.col, player.token))

    new GameState(newBoard, Some(move))
  }

  // TODO: Col and column are ambiguous, need better names.
  def findRow(column: List[Char], row: Int, col: Int, player: Player): Move = {

    // Column is full if row < 0
    // Return the <0 row to signify it's full
    if (row < 0) {
      return new Move(player, row, col)
    }

    if (column(row) == emptySpaceC) {
      new Move(player, row, col)
    }
    else {
      findRow(column, row-1, col, player)
    }

  }

}
