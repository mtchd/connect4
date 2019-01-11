import Main.{testBoardCols, testBoardRows}

/**
  * Represents a discrete state of the game.
  * @param board Connect 4 board, represented as characters for each cell.
  * @param lastMove Last move played, important for checking if game is won.
  */
// Could be 2d array not list of lists. Would be more efficient implementation. Perhaps vector?
class GameState(val board: List[List[Cell]], lastMove: Option[Move], val challenger: Player, val defender: Player,
                val channel: String, defendersTurn: Boolean) {

  // Extract number of columns and rows
  // More efficient to pass in class constructor, but this keeps code a little cleaner I think.
  val nBoardCols: Int = board.head.length
  val nBoardRows: Int = board.length

  // For constructing brand new board
  def this(boardRows: Int, boardCols: Int, challenger: Player, defender: Player, channel: String, defendersTurn: Boolean) =
    this(
      List.fill(boardRows)(List.fill(boardCols)(new Cell(Strings.emptySpace))),
      None,
      challenger,
      defender,
      channel,
      defendersTurn
    )

  // Blank debugging game state
  def this() =
    this(
      List.fill(testBoardRows)(List.fill(testBoardCols)(new Cell(Strings.emptySpace))),
      None,
      // Could be replaced with real stuff so tests come up
      new Player("X", ":blue_circle:"),
      new Player("O", ":red_circle:"),
      " ",
      true
    )

  /**
    * Print board to console, for debugging.
    */
  def printBoard(): Unit = {

    println("Game Board:")

    // Add column numbers above columns
    val annotatedBoard = "0123456789".take(nBoardCols) :: board

    for (line <- annotatedBoard) println(line)
  }

  def boardAsString(): String = {
    // Need to multiply boardCols by 2 because the emojis are two characters each
    val markers = Strings.colMarkers.take(nBoardCols*2)
    // Makes each row into string
    val stringBoard = board.map( x => x.mkString(""))
    // Constructs whole board with markers as string
    "\nGame Board:\n" + markers + "\n" + stringBoard.mkString("\n") + "\n" + markers
  }

  // TODO: Get rid of copied code
  /**
    * Overloaded board as string. Allows for putting a foreign board in. Maybe this would be better off if in Strings?
    * @param board Foreign board, but with same number of columns and rows.
    * @return Board represented as string (with emojis!)
    */
  def boardAsString(board: List[List[Cell]]): String = {
    // Need to multiply boardCols by 2 because the emojis are two characters each
    val markers = Strings.colMarkers.take(nBoardCols*2)
    // Makes each row into string
    val stringBoard = board.map( x => x.mkString(""))
    // Constructs whole board with markers as string
    "\nGame Board:\n" + markers + "\n" + stringBoard.mkString("\n") + "\n" + markers
  }

  /**
    * Checks if a player has won.
    * @return Winning player, or None if there is no winner yet.
    */
  def checkWin(): Option[Player] = {

    val move = lastMove.getOrElse{ return None }

    // Had to extract this here due to list access syntax being same as function syntax, i.e you can't put brackets
    // after transpose otherwise it thinks you're feeding it variables.
    val transposedBoard = board.transpose

    // Return the row that is responsible for the win...we need to make a new board...
    // So we gotta make a new row with the 4 offending characters updated...
    // That means if we have an index of the first of the 4 and the direction we should iterate along, we should be able
    // to get it use updated on them.
    // This is a little harder on the diagonal
    // So use getDiagonal with some modified variables to get...
    // Use new iterator like fourInARow that goes through, but replaces at index...
    // Probably a better way but this'll do.

    val horizontal = fourInARow(board(move.row), move.player.token, 0)

    if (horizontal.isDefined) {
      // Holy moly magic numbers
      val newBoard = replaceCells(move.row, horizontal.get, 0, 3, 1 )
      SlackClient.messageUser(boardAsString(newBoard), channel, move.player.slackId)
      return Some(move.player)
    }

    // There's got to be a nicer way of writing this.
    // TODO: Get rid of magic numbers or explain them
    if (fourInARow(board(move.row), move.player.token, 0).isDefined ||
      fourInARow(transposedBoard(move.col), move.player.token, 0).isDefined ||
      fourInARow(getDiagonal(move.row, move.col, -3, 1), move.player.token, 0).isDefined ||
      fourInARow(getDiagonal(move.row, move.col, -3, -1), move.player.token, 0).isDefined)
    {
      // TODO: Turn winning 4 tokens into medals

      // So we need the index first. Then, if it's horizontal, we take that row and replace four at the start index.

      // This may not be the best place to do this, perhaps we should send back the gameState with the winning player
      // attached instead of sending the winning player and printing here.
      SlackClient.messageUser(boardAsString(), channel, move.player.slackId)
      Some(move.player)
    }
    else
    {
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
    * @param direction -1 if southeast diagonal, 1 for vertical, 1 for northeast diagonal, 0 for horizontal
    * @param offset Offset from starting cell in positive direction
    * @param zeroIfVertical Set this to zero if horizontal, or one if anything else.
    * @return
    */
  def replaceCells(startRow: Int, startCol: Int, direction: Int, offset: Int, zeroIfVertical: Int): List[List[Cell]] = {

    if (offset == 0) {
      //Stop and return
      println("Replacing cell at origin.")
      val newBoard = replaceCell(board, startRow, startCol, Strings.winningToken)
      println(boardAsString(newBoard))
      newBoard
    }
    // Fails hard if goes out of bounds, but something has gone seriously wrong if that happens.
    else {
      println("Replacing cell elsewhere.")
      val updatedboard = replaceCells(startRow, startCol, direction, offset - 1, zeroIfVertical)
      println(boardAsString(updatedboard))
      val newBoard = replaceCell(
        updatedboard,
        startRow + (offset*direction),
        startCol + (offset*zeroIfVertical),
        // Hardcoded replacement until otherwise needed
        Strings.winningToken)
      println(boardAsString(newBoard))
      newBoard
    }
  }

  def replaceCell(oldBoard: List[List[Cell]], row: Int, col: Int, token: String): List[List[Cell]] = {
    // This is creating a new row with one character updated, then created a new board with one row updated.
    oldBoard.updated(row,
      oldBoard(row).updated(col, new Cell(token)))
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

  // TODO: Rediscover that algorithm for diagonals instead of this manual stuff
  /**
    * Get cells along a diagonal. If it goes off the board, it treats them as empty cells.
    * @param row Row number of centre cell
    * @param col Column number of centre cell
    * @param offset current offset from centre cell, along diagonal.
    * @param NEorSE Northeast or Southeast. Refers to diagonal we are along, with north being up a column.
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

  def playMove(col: Int, playerId: String): Option[GameState] = {

    // Check it's this players turn
    // TODO: Some non-functional stuff here, needs to be changed.
    var optionPlayer: Option[Player] = None

    if (!defendersTurn && challenger.slackId == playerId) {
      optionPlayer = Some(challenger)
    }
    else if (defendersTurn && defender.slackId == playerId) {
      optionPlayer = Some(defender)
    }

    val player = optionPlayer.getOrElse {
      SlackClient.messageUser("It's not your turn.", channel, playerId)
      return None
    }

    // Check col is valid
    if (col < 0 || col > nBoardCols - 1) {
      SlackClient.messageUser("Col is out of bounds.", channel, playerId)
      return None
    }

    // Get corresponding column
    val transposedBoard = board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = findRow(transposedBoard(col), nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      SlackClient.messageUser("Column is full.", channel, playerId)
      return None
    }

    val newBoard = replaceCell(board, move.row, move.col, player.token)

    Some(new GameState(newBoard, Some(move), challenger, defender, channel, !defendersTurn))
  }

  // TODO: Col and column are ambiguous, need better names.
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
      findRow(column, row-1, col, player)
    }

  }

}
