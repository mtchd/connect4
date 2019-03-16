object GameState {

  // Hardcoded crap
  val DefaultBoardCols = 6
  val DefaultBoardRows = 7

  // Constants for going directions in replaceCells.
  val UpperRight: (Int, Int) = (-1, 1)
  val LowerRight: (Int, Int) = (1, 1)
  val Horizontal: (Int, Int) = (0, 1)
  val Vertical: (Int, Int) = (1, 0)

  def newDefaultBoard(): GameState = {
    newCustomBoard(GameState.DefaultBoardRows, GameState.DefaultBoardCols)
  }

  def newCustomBoard(boardRows: Int, boardCols: Int): GameState = {
    GameState(List.fill(boardRows)(List.fill(boardCols)(Cell(Empty))), None)
  }

}

/**
  * Represents a discrete state of the game.
  * @param board Connect 4 board, represented as characters for each cell.
  * @param lastMove Last move played, important for checking if game is won.
  */
// Could be 2d array not list of lists. Would be more efficient implementation. Perhaps vector?
case class GameState(board: List[List[Cell]], lastMove: Option[Move]) {

  // Extract number of columns and rows
  // More efficient to pass in class constructor, but this keeps code a little cleaner I think.
  val nBoardCols: Int = board.head.length
  val nBoardRows: Int = board.length

  def boardAsString(
                     defenderToken: String,
                     challengerToken: String,
                     winningToken: String,
                     emptyToken: String,
                     colMarkers: String
                   )
  : String = {

    // Makes each row into string
    val stringBoard = board.map{ row =>
      row.map { cell =>
        cell.contents match {
          case Challenger => challengerToken
          case Defender => defenderToken
          case Winner => winningToken
          case _ => emptyToken
        }
        // Rows become strings, not lists
      }.mkString("")
    }
    // Constructs whole board with markers on top and bottom
    "\nGame Board:\n" + colMarkers + "\n" + stringBoard.mkString("\n") + "\n" + colMarkers
  }

  def boardAsString(defender: Player, challenger: Player): String = {
    boardAsString(
      defender.token,
      challenger.token,
      Strings.WinningToken,
      Strings.EmptySpace,
      // Need to multiply boardCols by 2 because the emojis are two characters each
      Strings.ColMarkers.take(nBoardCols*2))
  }

  // TODO: Maybe this should override toString
  // For console printing
  def boardAsString(): String = {
    boardAsString(
      Strings.ConsoleDefenderToken,
      Strings.ConsoleChallengerToken,
      Strings.ConsoleWinningToken,
      Strings.ConsoleEmptySpace,
      Strings.ConsoleColMarkers.take(nBoardCols)
    )
  }

  // This is really where the win check happens
  def maybeWinningBoard(): Option[GameState] = {

    val move = lastMove.getOrElse{ return None }

    val playerRole = move.playerRole

    // Had to extract this here due to list access syntax being same as function syntax, i.e you can't put brackets
    // after transpose otherwise it thinks you're feeding it variables.
    val transposedBoard = board.transpose

    // TODO: Make this less arcane
    // This is pretty complex. Essentially, we check each of the 4 directions (e.g. Up/down or diagonal up right/down
    // left) for a "connect 4" and then replace those cells with medals if true, returning that winning board.
    fourInARow(board(move.row), playerRole)
      .map(horizontal => replaceCells(move.row, horizontal, GameState.Horizontal))

      .orElse(
        fourInARow(transposedBoard(move.col), playerRole)
          .map(vertical => replaceCells(vertical, move.col, GameState.Vertical))
      )
      // TODO: Still magic numbers left
      // Index starts at -3 along diagonals, because that's the offset diagonally from the move, so when we return the
      // index, we know it's the start of our four in a row.
      .orElse(
        fourInARow(getDiagonal(move.row, move.col, -1), playerRole)
          .map(upperRightDiag => replaceCells(move.row - (upperRightDiag - 3), move.col + (upperRightDiag - 3), GameState.UpperRight))
      )
      .orElse(
        fourInARow(getDiagonal(move.row, move.col, 1), playerRole)
          .map(lowerRightDiag => replaceCells(move.row + lowerRightDiag - 3, move.col + lowerRightDiag - 3, GameState.LowerRight))
      )

  }

  def replaceCells(startRow: Int, startCol: Int, direction: (Int,Int)): GameState = {
    replaceCells(startRow, startCol, direction, 3, Winner)
  }

  def replaceCells(startRow: Int, startCol: Int, direction: (Int,Int), token: CellContents): GameState = {
    replaceCells(startRow, startCol, direction, 3, token)
  }

  // Replacing the whole board with each update is painful, but seems to be necessary here.
  // Offset starts at 3 and goes down for replacing the classic 4 cells
  // TODO: Just have a simple enum to specify direction then give direction/zeroIfHorizontal values
  /**
    * Replaces multiple cells, along diagonal, vertical, or horizontal.
    * @param startRow Row number of starting cell
    * @param startCol Column number of starting cell
    * @param direction -1 if northeast diagonal, 1 for vertical, 1 for southeast diagonal, 0 for horizontal.
    *                  The 2nd number is 0 if vertical or 1 otherwise.
    * @param offset Offset from starting cell in positive direction. Starts at 3 and counts down usually.
    * @param newToken New token to replace the current cells tokens with.
    * @return Updated board
    */
  // How do I format this the right way? Or use less parameters...
  def replaceCells(startRow: Int, startCol: Int, direction: (Int,Int), offset: Int, newToken: CellContents
                  ): GameState = {

    if (offset == 0) {
      //Stop and return
      replaceCell(this, startRow, startCol, newToken)
    }
    // Fails hard if goes out of bounds, but something has gone seriously wrong if that happens. (So we want it to.)
    else {
      replaceCell(
        replaceCells(startRow, startCol, direction, offset - 1, newToken),
        startRow + (offset*direction._1),
        startCol + (offset*direction._2),
        newToken)
    }
  }

  def replaceCell(gameState: GameState, row: Int, col: Int, newToken: CellContents): GameState = {
    // This is creating a new row with one character updated, then created a new board with one row updated.
    // Should be a cleaner way of doing this.
    val oldBoard = gameState.board
    updateBoardOnly(oldBoard.updated(row, oldBoard(row).updated(col, Cell(newToken))))
  }

  // Keeping hold of this in case there are more variables in constructor added later, makes life easier
  def updateBoardOnly(newBoard: List[List[Cell]]): GameState = {
    new GameState(newBoard, lastMove)
  }

  def updateLastMoveOnly(move: Option[Move]): GameState = {
    new GameState(board, move)
  }

  /**
    * Checks a list of game board cells for 4 of the players token in a row.
    * @param cells List of cells to check.
    * @param playerToken Token of player we are checking has 4 in a row.
    * @return Index of start cell of the 4 in a row, if there is 4 in a row. Otherwise none.
    */
  def fourInARow(cells: List[Cell], playerToken: CellContents): Option[Int] = {

    val index = cells.indexOfSlice(List.fill(4)(Cell(playerToken)))

    if ( index >= 0 ) {
      Some(index)
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
      case e: IndexOutOfBoundsException => Cell(Empty)
    }
  }

  // TODO: Col and column are ambiguous, need better names.
  // TODO: Could potentially put this in the companion object
  def findRow(column: List[Cell], row: Int, col: Int, player: CellContents): Move = {

    // Column is full if row < 0
    // Return the <0 row to signify it's full
    if (row < 0) {
      return Move(player, row, col)
    }

    if (column(row).contents == Empty) {
      Move(player, row, col)
    }
    else {
      findRow(column, row - 1, col, player)
    }

  }

}