/**
  * Represents a discrete state of the game.
  * @param board Connect 4 board, represented as characters for each cell.
  * @param lastMove Last move played, important for checking if game is won.
  */
// Could be 2d array not list of lists. Would be more efficient implementation. Perhaps vector?
class GameState(val board: List[List[Cell]], val lastMove: Option[Move]) {

  // Extract number of columns and rows
  // More efficient to pass in class constructor, but this keeps code a little cleaner I think.
  val nBoardCols: Int = board.head.length
  val nBoardRows: Int = board.length

  // For constructing brand new board
  def this(boardRows: Int, boardCols: Int) =
    this(
      List.fill(boardRows)(List.fill(boardCols)(new Cell(Strings.emptySpace))),
      None
    )

  // Builds new board with default board rows and cols
  def this() = this(GameState.defaultBoardRows, GameState.defaultBoardCols)

  /**
    * Print board to console, for debugging.
    */
  def printBoard(): Unit = {

    println("Game Board:")

    // Add column numbers above columns
    val annotatedBoard = "0123456789".take(nBoardCols) :: board

    for (line <- annotatedBoard) println(line)
  }

  // TODO: Maybe this should override toString
  def boardAsString(): String = {
    // Need to multiply boardCols by 2 because the emojis are two characters each
    val markers = Strings.colMarkers.take(nBoardCols*2)
    // Makes each row into string
    val stringBoard = board.map( x => x.mkString(""))
    // Constructs whole board with markers as string
    "\nGame Board:\n" + markers + "\n" + stringBoard.mkString("\n") + "\n" + markers
  }

  // This is really where the win check happens
  def maybeWinningBoard(): Option[GameState] = {

    val move = lastMove.getOrElse{ return None }

    // Had to extract this here due to list access syntax being same as function syntax, i.e you can't put brackets
    // after transpose otherwise it thinks you're feeding it variables.
    val transposedBoard = board.transpose

    // TODO: So many damn magic numbers
    // Must be a better way
    val horizontal = fourInARow(board(move.row), move.player.token)
    val vertical = fourInARow(transposedBoard(move.col), move.player.token)
    // Index starts at -3 along diagonals, because that's the offset diagonally from the move, so when we return the
    // index, we know it's the start of our four in a row.
    val upperRightDiag = fourInARow(getDiagonal(move.row, move.col, -1), move.player.token, -3)
    val lowerRightDiag = fourInARow(getDiagonal(move.row, move.col, 1), move.player.token, -3)

    // This should be a switch statement
    if (horizontal.isDefined) {
      // Holy moly magic numbers
      Some(replaceCells(move.row, horizontal.get, 0, 3, 1 ,Strings.winningToken))
    }
    else if (vertical.isDefined) {
      Some(replaceCells(move.row, vertical.get, 1, 3, 0, Strings.winningToken ))
    }
    else if (upperRightDiag.isDefined) {
      Some(replaceCells(move.row - upperRightDiag.get, move.col + upperRightDiag.get, 1, 3, 1, Strings.winningToken ))
    }
    else if (lowerRightDiag.isDefined) {
      Some(replaceCells(move.row + lowerRightDiag.get, move.col + lowerRightDiag.get, 1, 3, 1, Strings.winningToken ))
    }

    else {
      None
    }
  }

  // Replacing the whole board with each update is painful, but seems to be necessary here.
  // Offset starts at 3 and goes down for replacing the classic 4 cells
  // TODO: Just have a simple enum to specify direction then give direction/zeroIfHorizontal values
  /**
    * Replaces multiple cells, along diagonal, vertical, or horizontal.
    * @param startRow Row number of starting cell
    * @param startCol Column number of starting cell
    * @param direction -1 if northeast diagonal, 1 for vertical, 1 for southeast diagonal, 0 for horizontal
    * @param offset Offset from starting cell in positive direction. Starts at 3 and counts down usually.
    * @param zeroIfVertical Set this to zero if horizontal, or one if anything else.
    * @param newToken New token to replace the current cells tokens with.
    * @return Updated board
    */
  // How do I format this the right way? Or use less parameters...
  def replaceCells(startRow: Int, startCol: Int, direction: Int, offset: Int, zeroIfVertical: Int, newToken: String
                  ): GameState = {

    if (offset == 0) {
      //Stop and return
      replaceCell(this, startRow, startCol, newToken)
    }
    // Fails hard if goes out of bounds, but something has gone seriously wrong if that happens.
    else {
      replaceCell(
        replaceCells(startRow, startCol, direction, offset - 1, zeroIfVertical, newToken),
        startRow + (offset*direction),
        startCol + (offset*zeroIfVertical),
        newToken)
    }
  }

  def replaceCell(gameState: GameState, row: Int, col: Int, newToken: String): GameState = {
    // This is creating a new row with one character updated, then created a new board with one row updated.
    // Should be a cleaner way of doing this.
    val oldBoard = gameState.board
    updateBoardOnly(oldBoard.updated(row, oldBoard(row).updated(col, new Cell(newToken))))
  }

  // Keeping hold of this in case there are more variables in constructor added later, makes life easier
  def updateBoardOnly(newBoard: List[List[Cell]]): GameState = {
    new GameState(newBoard, lastMove)
  }

  def updateLastMoveOnly(move: Option[Move]): GameState = {
    new GameState(board, move)
  }

  // Index always starts at zero
  def fourInARow(cells: List[Cell], playerToken: String): Option[Int] = {
    fourInARow(cells, playerToken, 0)
  }

  /**
    * Checks an abritary list of ordered cells if there is 4 in a row. O(n) complexity. (O(n*4) in exact terms)
    * @param cells List of cells to check.
    * @param playerToken Token of player we are checking has 4 in a row.
    * @return Index of start cell of the 4 in a row, if there is 4 in a row. Otherwise none.
    */
  def fourInARow(cells: List[Cell], playerToken: String, index: Int): Option[Int] = {

    val firstFour = cells.take(4)

    if (firstFour.forall(x => x.contents == playerToken)) {
      Some(index)
    }
    else if (cells.length > 4) {
      fourInARow(cells.tail, playerToken, index + 1)
    }
    else {
      None
    }
  }

  // Starts with -3 default offset
  def getDiagonal(row: Int, col: Int, NEorSE: Int): List[Cell] = {
    getDiagonal(row, col, -3, NEorSE)
  }

  // TODO: Rediscover that algorithm for diagonals instead of this manual stuff
  /**
    * Get cells along a diagonal, within the offset number. If it goes off the board, it treats them as empty cells.
    * @param row Row number of centre cell (where last move was played)
    * @param col Column number of centre cell
    * @param offset current offset from centre cell, along diagonal. Usually starts at -3.
    * @param NEorSE 1 is southeast (right and down / left and up ). -1 is opposite.
    * @return List of cells that were along that diagonal, seven long.
    */
  def getDiagonal(row: Int, col: Int, offset: Int, NEorSE: Int): List[Cell] = {

    if (offset == 3) {
      List(safeGet(row + (offset*NEorSE), col + offset))
    }
    else {
      safeGet(row + (offset*NEorSE), col + offset) :: getDiagonal(row, col, offset + 1, NEorSE)
    }
  }

  def safeGet(row: Int, col: Int): Cell = {
    try {
      board(row)(col)
    } catch {
      // Empty cells can either be None or actual characters. I have chosen actual characters because it's easier to
      // print.
      case e: IndexOutOfBoundsException => new Cell(Strings.emptySpace)
    }
  }

  // TODO: Col and column are ambiguous, need better names.
  // TODO: Static functions like this should be delcared static in some way...will look up how that works in scala later
  def findRow(column: List[Cell], row: Int, col: Int, player: Player): Move = {

    // Column is full if row < 0
    // Return the <0 row to signify it's full
    if (row < 0) {
      return new Move(player, row, col)
    }

    if (column(row).contents == Strings.emptySpace) {
      new Move(player, row, col)
    }
    else {
      findRow(column, row - 1, col, player)
    }

  }

}

object GameState {

  // Hardcoded crap
  val defaultBoardCols = 6
  val defaultBoardRows = 7

}
