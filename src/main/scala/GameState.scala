import Main.emptySpace
import SlackClient.userError

/**
  * Represents a discrete state of the game.
  * @param board Connect 4 board, represented as characters for each cell.
  * @param lastMove Last move played, important for checking if game is won.
  */
// Could be 2d array not list of lists. Would be more efficient implementation. Perhaps vector?
class GameState(val board: List[List[Cell]], lastMove: Option[Move], val challenger: Player, val defender: Player) {

  // Extract number of columns and rows
  // More efficient to pass in class constructor, but this keeps code a little cleaner I think.
  val nBoardCols: Int = board.head.length
  val nBoardRows: Int = board.length

  // For constructing brand new board
  def this(boardRows: Int, boardCols: Int, challenger: Player, defender: Player) =
    this(List.fill(boardRows)(List.fill(boardCols)( new Cell(emptySpace))), None, challenger, defender)

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

  // Most efficient implementation: Search around latest token for match
  def checkWin(): Option[Player] = {

    // TODO: Check if this returns none on checkWin or move
    val move = lastMove.getOrElse{ return None }

    // Had to extract this here due to list access syntax being same as function syntax, i.e you can't put brackets
    // after transpose otherwise it thinks you're feeding it variables.
    val transposedBoard = board.transpose

    // There's got to be a nicer way of writing this.
    // TODO: Get rid of magic numbers or explain them
    if (fourInARow(board(move.row), move.player.token) ||
      fourInARow(transposedBoard(move.col), move.player.token) ||
      fourInARow(getDiagonal(move.row, move.col, -3, 1), move.player.token) ||
      fourInARow(getDiagonal(move.row, move.col, -3, -1), move.player.token))
    {
      Some(move.player)
    }
    else
    {
      None
    }

  }

  // Return the cells for given col+row which need to be checked to verify if we
  // can have 4 in a row horizontally.
  def fourInARow(row: List[Cell], playerToken: String): Boolean = {

    val firstFour = row.take(4)

    if (firstFour.forall(x => x.contents == playerToken)) {
      true
    }
    else if (row.length > 4) {
      fourInARow(row.tail, playerToken)
    }
    else {
      false
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
      case e: IndexOutOfBoundsException => new Cell(emptySpace)
    }
  }

  def playMove(col: Int, player: Player): GameState = {

    // Check col is valid
    if (col < 0 || col > nBoardCols - 1) {
      userError("Col is out of bounds.")
      return this
    }

    // Get corresponding column
    val transposedBoard = board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = findRow(transposedBoard(col), nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      userError("Column is full.")
      return this
    }

    // TODO: This is messy and hard to read, please fix.
    // This is creating a new row with one character updated, then created a new board with one row updated.
    val newBoard = board.updated(move.row, board(move.row).updated(move.col, new Cell(player.token)))

    new GameState(newBoard, Some(move), challenger, defender)
  }

  // TODO: Col and column are ambiguous, need better names.
  def findRow(column: List[Cell], row: Int, col: Int, player: Player): Move = {

    // Column is full if row < 0
    // Return the <0 row to signify it's full
    if (row < 0) {
      return new Move(player, row, col)
    }

    if (column(row).contents == emptySpace) {
      new Move(player, row, col)
    }
    else {
      findRow(column, row-1, col, player)
    }

  }

}
